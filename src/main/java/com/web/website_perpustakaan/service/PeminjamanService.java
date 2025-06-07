package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.repository.PeminjamanRepository;
import com.web.website_perpustakaan.repository.BukuRepository;
import com.web.website_perpustakaan.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeminjamanService {

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private BukuRepository bukuRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DendaService dendaService;

    @Transactional
    public Peminjaman pinjamBuku(Long userId, Long bukuId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        Buku buku = bukuRepository.findById(bukuId)
                .orElseThrow(() -> new IllegalArgumentException("Buku tidak ditemukan"));

        if (buku.getStok() <= 0) {
            throw new IllegalArgumentException("Stok buku habis");
        }

        if (peminjamanRepository.findByUserUserIdAndStatusPeminjaman(userId, Peminjaman.StatusPeminjaman.DIPINJAM)
                .stream().anyMatch(p -> p.getBuku().getBukuId().equals(bukuId))) {
            throw new IllegalArgumentException("Buku sudah dipinjam");
        }

        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setUser(user);
        peminjaman.setBuku(buku);
        peminjaman.setTanggalPeminjaman(LocalDate.now());
        peminjaman.setTanggalPengembalian(LocalDate.now().plusDays(7));
        peminjaman.setStatusPeminjaman(Peminjaman.StatusPeminjaman.DIPINJAM);

        buku.setStok(buku.getStok() - 1);
        bukuRepository.save(buku);

        return peminjamanRepository.save(peminjaman);
    }

    @Transactional
    public Peminjaman kembalikanBuku(Long peminjamanId) {
        Peminjaman peminjaman = peminjamanRepository.findById(peminjamanId)
                .orElseThrow(() -> new IllegalArgumentException("Peminjaman tidak ditemukan"));

        if (peminjaman.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.DIPINJAM &&
            peminjaman.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.TERLAMBAT) {
            throw new IllegalArgumentException("Buku sudah dikembalikan");
        }

        LocalDate returnDate = LocalDate.now();
        peminjaman.setTanggalDikembalikan(returnDate);

        if (returnDate.isAfter(peminjaman.getTanggalPengembalian())) {
            peminjaman.setStatusPeminjaman(Peminjaman.StatusPeminjaman.TERLAMBAT);
            Denda denda = dendaService.hitungDenda(peminjaman);
            if (denda != null) {
                peminjaman.setDenda(denda);
            }
        } else {
            peminjaman.setStatusPeminjaman(Peminjaman.StatusPeminjaman.DIKEMBALIKAN);
        }

        Buku buku = peminjaman.getBuku();
        buku.setStok(buku.getStok() + 1);
        bukuRepository.save(buku);

        return peminjamanRepository.save(peminjaman);
    }

    public List<Peminjaman> getPeminjamanAktif(Long userId) {
        if (userId == null) return Collections.emptyList();
        return peminjamanRepository.findByUserUserId(userId).stream()
                .filter(p -> p.getTanggalDikembalikan() == null)
                .peek(p -> {
                    if (LocalDate.now().isAfter(p.getTanggalPengembalian()) &&
                        p.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.TERLAMBAT) {
                        p.setStatusPeminjaman(Peminjaman.StatusPeminjaman.TERLAMBAT);
                        Denda denda = dendaService.hitungDenda(p);
                        if (denda != null) {
                            p.setDenda(denda);
                        }
                        peminjamanRepository.save(p);
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Peminjaman> getRiwayatPeminjaman(Long userId) {
        if (userId == null) return Collections.emptyList();
        return peminjamanRepository.findByUserUserId(userId).stream()
                .filter(p -> p.getTanggalDikembalikan() != null)
                .collect(Collectors.toList());
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanRepository.findAll();
    }
}
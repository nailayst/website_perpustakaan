package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.repository.PeminjamanRepository;
import com.web.website_perpustakaan.repository.BukuRepository;
import com.web.website_perpustakaan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PeminjamanService {

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Autowired
    private BukuRepository bukuRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Peminjaman pinjamBuku(Long userId, Long bukuId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        Buku buku = bukuRepository.findById(bukuId)
                .orElseThrow(() -> new IllegalArgumentException("Buku tidak ditemukan"));

        if (buku.getStok() <= 0) {
            throw new IllegalArgumentException("Stok buku habis");
        }

        // Cek apakah user sudah meminjam buku ini
        boolean sudahDipinjam = peminjamanRepository.findByUserUserIdAndStatusPeminjaman(userId, Peminjaman.StatusPeminjaman.DIPINJAM)
                .stream()
                .anyMatch(p -> p.getBuku().getBukuId().equals(bukuId));
        if (sudahDipinjam) {
            throw new IllegalArgumentException("Buku sudah dipinjam oleh user ini");
        }

        Peminjaman peminjaman = new Peminjaman();
        peminjaman.setUser(user);
        peminjaman.setBuku(buku);
        peminjaman.setTanggalPeminjaman(LocalDate.now());
        peminjaman.setTanggalPengembalian(LocalDate.now().plusDays(7));
        peminjaman.setStatusPeminjaman(Peminjaman.StatusPeminjaman.DIPINJAM);

        // Kurangi stok buku
        buku.setStok(buku.getStok() - 1);
        bukuRepository.save(buku);

        return peminjamanRepository.save(peminjaman);
    }

    @Transactional
    public Peminjaman kembalikanBuku(Long peminjamanId) {
        Peminjaman peminjaman = peminjamanRepository.findById(peminjamanId)
                .orElseThrow(() -> new IllegalArgumentException("Peminjaman tidak ditemukan"));

        if (peminjaman.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.DIPINJAM) {
            throw new IllegalArgumentException("Buku sudah dikembalikan atau status tidak valid");
        }

        peminjaman.setTanggalDikembalikan(LocalDate.now());
        peminjaman.setStatusPeminjaman(LocalDate.now().isAfter(peminjaman.getTanggalPengembalian())
                ? Peminjaman.StatusPeminjaman.TERLAMBAT
                : Peminjaman.StatusPeminjaman.DIKEMBALIKAN);

        // Tambah stok buku
        Buku buku = peminjaman.getBuku();
        buku.setStok(buku.getStok() + 1);
        bukuRepository.save(buku);

        return peminjamanRepository.save(peminjaman);
    }

    public List<Peminjaman> getPeminjamanAktif(Long userId) {
        return peminjamanRepository.findByUserUserIdAndStatusPeminjaman(userId, Peminjaman.StatusPeminjaman.DIPINJAM);
    }

    public List<Peminjaman> getRiwayatPeminjaman(Long userId) {
        return peminjamanRepository.findByUserUserId(userId);
    }

    public List<Peminjaman> getAllPeminjaman() {
        return peminjamanRepository.findAll();
    }
}
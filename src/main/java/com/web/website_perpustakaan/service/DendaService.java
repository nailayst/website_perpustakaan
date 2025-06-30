package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.repository.DendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class DendaService {

    @Autowired
    private DendaRepository dendaRepository;

    private static final double DENDA_PER_HARI = 1000.0;

    @Transactional
    public Denda hitungDenda(Peminjaman peminjaman) {
        if (peminjaman == null || peminjaman.getStatusPeminjaman() == null || peminjaman.getPeminjamanId() == null) {
            return null;
        }

        Optional<Denda> existingDendaOptional = dendaRepository.findByPeminjamanId(peminjaman.getPeminjamanId());
        Denda denda = existingDendaOptional.orElse(new Denda());

        if (existingDendaOptional.isPresent() && denda.getStatusPembayaran() == Denda.StatusPembayaran.SUDAH_DIBAYAR) {
            return denda; 
        }

        if (peminjaman.getStatusPeminjaman() == Peminjaman.StatusPeminjaman.TERLAMBAT) {
            LocalDate tanggalPerbandingan = peminjaman.getTanggalDikembalikan() != null
                    ? peminjaman.getTanggalDikembalikan()
                    : LocalDate.now();

            if (peminjaman.getTanggalPengembalian() == null) {
                return null;
            }

            long hariTerlambat = ChronoUnit.DAYS.between(peminjaman.getTanggalPengembalian(), tanggalPerbandingan);

            if (hariTerlambat <= 0) {
                if (denda.getStatusPembayaran() == Denda.StatusPembayaran.BELUM_DIBAYAR) {
                    denda.setJumlahDenda(0.0);
                    denda.setStatusPembayaran(Denda.StatusPembayaran.SUDAH_DIBAYAR);
                    denda.setTanggalPembayaran(LocalDate.now());
                }
                return dendaRepository.save(denda);
            }

            denda.setPeminjamanId(peminjaman.getPeminjamanId());
            denda.setJumlahDenda((double) hariTerlambat * DENDA_PER_HARI);
            denda.setKeterangan("Terlambat " + hariTerlambat + " hari.");
            denda.setStatusPembayaran(Denda.StatusPembayaran.BELUM_DIBAYAR);
            denda.setTanggalDibuat(denda.getTanggalDibuat() == null ? LocalDate.now() : denda.getTanggalDibuat());
            denda.setTanggalPembayaran(null);
            
            return dendaRepository.save(denda);

        } else { 
            if (existingDendaOptional.isPresent() && denda.getStatusPembayaran() == Denda.StatusPembayaran.BELUM_DIBAYAR) {
                denda.setJumlahDenda(0.0);
                denda.setStatusPembayaran(Denda.StatusPembayaran.SUDAH_DIBAYAR);
                denda.setTanggalPembayaran(LocalDate.now());
                return dendaRepository.save(denda);
            }
            return null;
        }
    }


    @Transactional
    public Denda bayarDenda(Long dendaId) {
        Optional<Denda> optionalDenda = dendaRepository.findById(dendaId);
        Denda denda = optionalDenda.orElseThrow(() -> new IllegalArgumentException("Denda tidak ditemukan"));

        if (denda.getStatusPembayaran() == Denda.StatusPembayaran.SUDAH_DIBAYAR) {
            throw new IllegalArgumentException("Denda sudah dibayar");
        }
        denda.setStatusPembayaran(Denda.StatusPembayaran.SUDAH_DIBAYAR);
        denda.setTanggalPembayaran(LocalDate.now());
        return dendaRepository.save(denda);
    }

    public Denda getDendaById(Long dendaId) {
        return dendaRepository.findById(dendaId).orElse(null);
    }

    public Optional<Denda> getDendaByPeminjamanId(Long peminjamanId) {
        return dendaRepository.findByPeminjamanId(peminjamanId);
    }

    public List<Denda> getAllDenda() {
        return dendaRepository.findAll();
    }

    public List<Denda> getDendaBelumDibayarByUserId(Long userId) {
        return dendaRepository.findDendaByUserIdAndStatus(userId, Denda.StatusPembayaran.BELUM_DIBAYAR);
    }

    public long countAllDendaBelumDibayar() { 
        return dendaRepository.countByStatusPembayaran(Denda.StatusPembayaran.BELUM_DIBAYAR);
    }

    public List<Denda> getAllDendaByStatus(String status) {
        try {
            Denda.StatusPembayaran enumStatus = Denda.StatusPembayaran.valueOf(status.toUpperCase());
            return dendaRepository.findAllByStatusPembayaran(enumStatus);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Denda Status: " + status + ". Error: " + e.getMessage());
            return List.of();
        }
    }
}
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

@Service
public class DendaService {

    @Autowired
    private DendaRepository dendaRepository;

    private static final long DENDA_PER_HARI = 1000;

    @Transactional
    public Denda hitungDenda(Peminjaman peminjaman) {
        if (peminjaman == null || peminjaman.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.TERLAMBAT) {
            return null;
        }

        LocalDate tanggalPerbandingan = peminjaman.getTanggalDikembalikan() != null 
                ? peminjaman.getTanggalDikembalikan() 
                : LocalDate.now();
        long hariTerlambat = ChronoUnit.DAYS.between(peminjaman.getTanggalPengembalian(), tanggalPerbandingan);
        if (hariTerlambat <= 0) return null;

        Denda denda = peminjaman.getDenda();
        if (denda == null) {
            denda = new Denda();
            denda.setPeminjamanId(peminjaman.getPeminjamanId());
            denda.setStatusPembayaran(Denda.StatusPembayaran.BELUM_DIBAYAR);
        }
        denda.setJumlahDenda(hariTerlambat * DENDA_PER_HARI);
        return dendaRepository.save(denda);
    }

    @Transactional
    public Denda bayarDenda(Long dendaId) {
        Denda denda = dendaRepository.findById(dendaId)
                .orElseThrow(() -> new IllegalArgumentException("Denda tidak ditemukan"));
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

    public Denda getDendaByPeminjamanId(Long peminjamanId) {
        return dendaRepository.findByPeminjamanId(peminjamanId);
    }

    public List<Denda> getAllDenda() {
        return dendaRepository.findAll();
    }
}
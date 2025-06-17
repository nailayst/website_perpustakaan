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
        if (peminjaman == null || peminjaman.getStatusPeminjaman() == null) {
            return null;
        }

        if (peminjaman.getStatusPeminjaman() != Peminjaman.StatusPeminjaman.TERLAMBAT) {
            return null;
        }

        LocalDate tanggalPerbandingan = peminjaman.getTanggalDikembalikan() != null
                ? peminjaman.getTanggalDikembalikan()
                : LocalDate.now();

        if (peminjaman.getTanggalPengembalian() == null) {
            return null; 
        }

        long hariTerlambat = ChronoUnit.DAYS.between(peminjaman.getTanggalPengembalian(), tanggalPerbandingan);

        if (hariTerlambat <= 0) {
            return null;
        }

        Denda denda = peminjaman.getDenda(); 
        if (denda == null) {
            denda = new Denda();
            denda.setPeminjamanId(peminjaman.getPeminjamanId());
            denda.setStatusPembayaran(Denda.StatusPembayaran.BELUM_DIBAYAR); 
            denda.setTanggalPembayaran(null); 
        } else {
            if (denda.getStatusPembayaran() == Denda.StatusPembayaran.SUDAH_DIBAYAR) {
                 return denda; 
            }
        }

        denda.setJumlahDenda((double) hariTerlambat * DENDA_PER_HARI);
        return dendaRepository.save(denda);
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

    public Denda getDendaByPeminjamanId(Long peminjamanId) {
        return dendaRepository.findByPeminjamanId(peminjamanId);
    }

    public List<Denda> getAllDenda() {
        return dendaRepository.findAll();
    }
}
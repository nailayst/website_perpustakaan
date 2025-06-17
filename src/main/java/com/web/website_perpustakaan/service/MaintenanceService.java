package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.*;
import com.web.website_perpustakaan.model.Buku.StatusBuku; 
import com.web.website_perpustakaan.model.Peminjaman.StatusPeminjaman; 
import com.web.website_perpustakaan.model.Maintenance.*;
import com.web.website_perpustakaan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private BukuRepository bukuRepository; 

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    public Maintenance mulaiMaintenance(Long bukuId, JenisMaintenance jenisMaintenance, String keterangan) {
        Buku buku = bukuRepository.findById(bukuId)
                .orElseThrow(() -> new IllegalArgumentException("Buku dengan ID " + bukuId + " tidak ditemukan"));

        List<Peminjaman> peminjamanAktif = peminjamanRepository.findByBuku_BukuIdAndStatusPeminjaman(bukuId, StatusPeminjaman.DIPINJAM); 
        if (!peminjamanAktif.isEmpty()) {
            throw new IllegalArgumentException("Buku sedang dipinjam, tidak dapat dimaintenance.");
        }

        Maintenance maintenance = new Maintenance();
        maintenance.setBuku(buku);
        maintenance.setJenisMaintenance(jenisMaintenance);
        maintenance.setKeterangan(keterangan);
        maintenance.setTanggalMaintenance(LocalDate.now());
        
        if (jenisMaintenance == JenisMaintenance.PENARIKAN) {
            maintenance.setStatus(StatusMaintenance.DITARIK_PERMANEN);
            buku.setStatusBuku(StatusBuku.DITARIK); 
            buku.setStok(buku.getStok() - 1);
        } else if (jenisMaintenance == JenisMaintenance.PERBAIKAN) {
            maintenance.setStatus(StatusMaintenance.DALAM_PROSES);
            buku.setStatusBuku(StatusBuku.DALAM_PERBAIKAN); 
        }

        bukuRepository.save(buku);
        return maintenanceRepository.save(maintenance);
    }

    public Buku getBukuForMaintenanceEdit(Long maintenanceId, String keteranganSelesai) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance dengan ID " + maintenanceId + " tidak ditemukan"));

        if (maintenance.getStatus() == StatusMaintenance.DITARIK_PERMANEN) {
            throw new IllegalArgumentException("Maintenance penarikan sudah permanen, tidak dapat diubah.");
        }

        if (maintenance.getJenisMaintenance() == JenisMaintenance.PERBAIKAN) {
            maintenance.setStatus(StatusMaintenance.SELESAI); 
            if (keteranganSelesai != null && !keteranganSelesai.trim().isEmpty()) {
                maintenance.setKeterangan(keteranganSelesai);
            }
            maintenanceRepository.save(maintenance); 
            return maintenance.getBuku(); 
        } else {
            throw new IllegalArgumentException("Hanya maintenance berjenis PERBAIKAN yang dapat diselesaikan untuk edit buku.");
        }
    }

    public List<Maintenance> getAllMaintenance() {
        return maintenanceRepository.findAll();
    }

    public List<Maintenance> getMaintenanceByBukuId(Long bukuId) {
        return maintenanceRepository.findByBuku_BukuId(bukuId);
    }

    public List<Maintenance> getMaintenanceByStatus(StatusMaintenance status) {
        return maintenanceRepository.findByStatus(status);
    }
}
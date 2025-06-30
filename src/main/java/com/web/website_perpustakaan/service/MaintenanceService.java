package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.Buku;
import com.web.website_perpustakaan.model.Maintenance;
import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.model.Maintenance.JenisMaintenance;
import com.web.website_perpustakaan.model.Maintenance.StatusMaintenance;
import com.web.website_perpustakaan.model.Buku.KondisiBuku;
import com.web.website_perpustakaan.model.Buku.StatusBuku;
import com.web.website_perpustakaan.model.Peminjaman.StatusPeminjaman;
import com.web.website_perpustakaan.repository.BukuRepository;
import com.web.website_perpustakaan.repository.MaintenanceRepository;
import com.web.website_perpustakaan.repository.PeminjamanRepository;
import jakarta.transaction.Transactional; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private BukuRepository bukuRepository;

    @Autowired
    private PeminjamanRepository peminjamanRepository;

    @Transactional
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
            if (buku.getStok() > 0) {
                buku.setStok(buku.getStok() - 1);
            } else {
                // Opsional: Tangani jika stok sudah 0 dan masih mencoba menarik
            }
            buku.setKondisi(KondisiBuku.RUSAK_BERAT);
        } else if (jenisMaintenance == JenisMaintenance.PERBAIKAN) {
            maintenance.setStatus(StatusMaintenance.DALAM_PROSES);
            buku.setStatusBuku(StatusBuku.DALAM_PERBAIKAN);
            buku.setKondisi(KondisiBuku.RUSAK_RINGAN);
        }

        bukuRepository.save(buku);
        return maintenanceRepository.save(maintenance);
    }

    @Transactional
    public void selesaikanPerbaikanBuku(Long maintenanceId, String keteranganSelesai) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance dengan ID " + maintenanceId + " tidak ditemukan"));

        if (maintenance.getStatus() == StatusMaintenance.DITARIK_PERMANEN || maintenance.getStatus() == StatusMaintenance.SELESAI) {
            throw new IllegalArgumentException("Maintenance sudah tidak dalam proses atau sudah ditarik permanen, tidak dapat diselesaikan lagi.");
        }

        if (maintenance.getJenisMaintenance() == JenisMaintenance.PERBAIKAN) {
            maintenance.setStatus(StatusMaintenance.SELESAI);
            if (keteranganSelesai != null && !keteranganSelesai.trim().isEmpty()) {
                maintenance.setKeterangan(keteranganSelesai);
            }
            maintenanceRepository.save(maintenance);

            Buku buku = maintenance.getBuku();
            buku.setStatusBuku(StatusBuku.TERSEDIA);
            buku.setKondisi(KondisiBuku.BAIK);
            bukuRepository.save(buku);

        } else {
            throw new IllegalArgumentException("Hanya maintenance berjenis PERBAIKAN yang dapat diselesaikan untuk edit buku.");
        }
    }

    public List<Maintenance> getAllMaintenance() {
        return maintenanceRepository.findAll();
    }

    public Page<Maintenance> getAllMaintenancePaginated(Pageable pageable) {
        return maintenanceRepository.findAll(pageable); 
    }

    public Page<Maintenance> searchMaintenancePaginated(String keyword, Pageable pageable) {
        return maintenanceRepository.findByBuku_JudulContainingIgnoreCaseOrKeteranganContainingIgnoreCase(
            keyword, keyword, pageable
        );
    }

    public List<Maintenance> getMaintenanceByBukuId(Long bukuId) {
        return maintenanceRepository.findByBuku_BukuId(bukuId);
    }

    public List<Maintenance> getMaintenanceByStatus(StatusMaintenance status) {
        return maintenanceRepository.findByStatus(status);
    }

    public Optional<Maintenance> getMaintenanceById(Long id) {
        return maintenanceRepository.findById(id);
    }

    public long countByStatus(String status) {
        try {
            StatusMaintenance enumStatus = StatusMaintenance.valueOf(status.toUpperCase());
            return maintenanceRepository.countByStatus(enumStatus);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Maintenance Status: " + status + ". Error: " + e.getMessage());
            return 0;
        }
    }

    public long getTotalDalamMaintenance() {
        return maintenanceRepository.countByStatus(StatusMaintenance.DALAM_PROSES);
    }
}
package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Maintenance;
import com.web.website_perpustakaan.model.Maintenance.StatusMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; 
import java.util.List;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long> {
    List<Maintenance> findByBuku_BukuId(Long bukuId);
    List<Maintenance> findByStatus(StatusMaintenance status);
    long countByStatus(StatusMaintenance status);
    Page<Maintenance> findByBuku_JudulContainingIgnoreCaseOrKeteranganContainingIgnoreCase(String judulBuku, String keterangan, Pageable pageable);
}
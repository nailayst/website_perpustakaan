package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Pengusulan;
import com.web.website_perpustakaan.model.Pengusulan.StatusPengusulan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable; 

@Repository
public interface PengusulanRepository extends JpaRepository<Pengusulan, Long> {
    List<Pengusulan> findByUser_UserIdOrderByTanggalPengusulanDesc(Long userId);
    List<Pengusulan> findByStatusPengusulan(StatusPengusulan status);
    long countByStatusPengusulan(StatusPengusulan status);
    Page<Pengusulan> findByJudulBukuContainingIgnoreCaseOrUser_UsernameContainingIgnoreCase(String judulBuku, String username, Pageable pageable);
}
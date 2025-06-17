package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Pengusulan; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PengusulanRepository extends JpaRepository<Pengusulan, Long> {
    List<Pengusulan> findByUser_UserIdOrderByTanggalPengusulanDesc(Long userId);
    List<Pengusulan> findByStatusPengusulan(Pengusulan.StatusPengusulan status);
}
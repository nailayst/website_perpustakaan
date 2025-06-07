package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Denda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DendaRepository extends JpaRepository<Denda, Long> {
    Denda findByPeminjamanId(Long peminjamanId);
}
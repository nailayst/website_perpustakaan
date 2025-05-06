package com.web.website_perpustakaan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.web.website_perpustakaan.model.Validasi;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidasiRepository extends JpaRepository<Validasi, Long> {
    Validasi findByToken(String token);
}
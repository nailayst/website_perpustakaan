package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Buku;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BukuRepository extends JpaRepository<Buku, Long> {
    List<Buku> findAllByOrderByBukuIdAsc();
}
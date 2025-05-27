package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Peminjaman;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {
    List<Peminjaman> findByUserUserIdAndStatusPeminjaman(Long userId, Peminjaman.StatusPeminjaman status);
    List<Peminjaman> findByUserUserId(Long userId);
}
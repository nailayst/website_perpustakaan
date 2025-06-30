package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Peminjaman;
import com.web.website_perpustakaan.model.Peminjaman.StatusPeminjaman;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PeminjamanRepository extends JpaRepository<Peminjaman, Long> {
    List<Peminjaman> findByUserUserIdAndStatusPeminjaman(Long userId, Peminjaman.StatusPeminjaman status);
    List<Peminjaman> findByUserUserId(Long userId);
    List<Peminjaman> findByBuku_BukuIdAndStatusPeminjaman(Long bukuId, StatusPeminjaman statusPeminjaman);
    long countByStatusPeminjaman(Peminjaman.StatusPeminjaman status);
    List<Peminjaman> findByStatusPeminjaman(Peminjaman.StatusPeminjaman status);
    Page<Peminjaman> findByBuku_JudulContainingIgnoreCaseOrUser_UsernameContainingIgnoreCase(String judulBuku, String username, Pageable pageable);
}
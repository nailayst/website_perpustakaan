package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Buku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.domain.Page;     
import org.springframework.data.domain.Pageable; 

@Repository
public interface BukuRepository extends JpaRepository<Buku, Long> {
    List<Buku> findAllByOrderByBukuIdAsc(); 
    List<Buku> findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCase(
        String judul, String penulis, String penerbit, String isbn);   
    List<Buku> findTop5ByOrderByTanggalTerbitDesc();
    List<Buku> findByStatusBuku(Buku.StatusBuku statusBuku);
    boolean existsByKodeBuku(String kodeBuku);
    Page<Buku> findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCaseOrKodeBukuContainingIgnoreCase(
        String judul, String penulis, String penerbit, String isbn, String kodeBuku, Pageable pageable);
}
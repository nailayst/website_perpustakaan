package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Buku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BukuRepository extends JpaRepository<Buku, Long> {
    List<Buku> findAllByOrderByBukuIdAsc();
    List<Buku> findByJudulContainingIgnoreCaseOrPenulisContainingIgnoreCaseOrPenerbitContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            String judul, String penulis, String penerbit, String isbn);
    List<Buku> findTop5ByOrderByTanggalTerbitDesc(); 
    List<Buku> findByStatusBuku(Buku.StatusBuku statusBuku); 
}
package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.model.Denda.StatusPembayaran;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DendaRepository extends JpaRepository<Denda, Long> {
    Optional<Denda> findByPeminjamanId(Long peminjamanId);
    @Query("SELECT d FROM Denda d JOIN Peminjaman p ON d.peminjamanId = p.peminjamanId WHERE p.user.userId = :userId AND d.statusPembayaran = :status")
    List<Denda> findDendaByUserIdAndStatus(@Param("userId") Long userId, @Param("status") StatusPembayaran status);
    long countByStatusPembayaran(StatusPembayaran status);
    List<Denda> findAllByStatusPembayaran(StatusPembayaran status);
}
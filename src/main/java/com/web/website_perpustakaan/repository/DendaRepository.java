package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.Denda;
import com.web.website_perpustakaan.model.Denda.StatusPembayaran; // Import enum
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import Query jika diperlukan
import org.springframework.data.repository.query.Param; // Import Param jika diperlukan

import java.util.List;

public interface DendaRepository extends JpaRepository<Denda, Long> {
    Denda findByPeminjamanId(Long peminjamanId);

    @Query("SELECT d FROM Denda d JOIN Peminjaman p ON d.peminjamanId = p.peminjamanId WHERE p.user.userId = :userId AND d.statusPembayaran = :status")
    List<Denda> findDendaByUserIdAndStatus(@Param("userId") Long userId, @Param("status") StatusPembayaran status);
}
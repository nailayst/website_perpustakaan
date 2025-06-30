package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "denda")
public class Denda implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dendaId;

    @Column(name = "peminjaman_id", nullable = false)
    private Long peminjamanId;

    @Column(name = "jumlah_denda", nullable = false)
    private Double jumlahDenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pembayaran", nullable = false)
    private StatusPembayaran statusPembayaran;

    @Column(name = "tanggal_dibuat", nullable = false)
    private LocalDate tanggalDibuat;

    @Column(name = "tanggal_pembayaran")
    private LocalDate tanggalPembayaran;

    @Column(name = "keterangan")
    private String keterangan;

    public enum StatusPembayaran {
        BELUM_DIBAYAR, SUDAH_DIBAYAR
    }

    // Constructor default 
    public Denda() {
        this.tanggalDibuat = LocalDate.now(); 
        this.statusPembayaran = StatusPembayaran.BELUM_DIBAYAR; 
        this.jumlahDenda = 0.0; 
        this.keterangan = ""; 
    }

    // Getters and Setters
    public Long getDendaId() { return dendaId; }
    public void setDendaId(Long dendaId) { this.dendaId = dendaId; }

    public Long getPeminjamanId() { return peminjamanId; }
    public void setPeminjamanId(Long peminjamanId) { this.peminjamanId = peminjamanId; }

    public Double getJumlahDenda() { return jumlahDenda; }
    public void setJumlahDenda(Double jumlahDenda) { this.jumlahDenda = jumlahDenda; }

    public LocalDate getTanggalDibuat() { return tanggalDibuat; }
    public void setTanggalDibuat(LocalDate tanggalDibuat) { this.tanggalDibuat = tanggalDibuat; }

    public LocalDate getTanggalPembayaran() { return tanggalPembayaran; }
    public void setTanggalPembayaran(LocalDate tanggalPembayaran) { this.tanggalPembayaran = tanggalPembayaran; }

    public StatusPembayaran getStatusPembayaran() { return statusPembayaran; }
    public void setStatusPembayaran(StatusPembayaran statusPembayaran) { this.statusPembayaran = statusPembayaran; }

    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
}
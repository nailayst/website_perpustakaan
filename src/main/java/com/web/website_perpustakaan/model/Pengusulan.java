package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "pengusulan")
public class Pengusulan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pengusulan_id") 
    private Long idPengusulan; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Judul buku tidak boleh kosong")
    @Size(max = 255, message = "Judul maksimal 255 karakter")
    @Column(name = "judul_buku", nullable = false)
    private String judulBuku;

    @Size(max = 255, message = "Penulis maksimal 255 karakter")
    @Column(name = "penulis")
    private String penulis;

    @Size(max = 255, message = "Penerbit maksimal 255 karakter")
    @Column(name = "penerbit")
    private String penerbit;

    @Size(max = 50, message = "Kategori maksimal 50 karakter")
    @Column(name = "kategori")
    private String kategori;

    @Min(value = 1000, message = "Tahun terbit tidak valid")
    @Max(value = 9999, message = "Tahun terbit tidak valid")
    @Column(name = "tahun_terbit")
    private Integer tahunTerbit;

    @Size(max = 500, message = "Keterangan usulan maksimal 500 karakter")
    @Column(name = "keterangan_pengusulan", length = 500)
    private String keteranganPengusulan;

    @Column(name = "tanggal_pengusulan", nullable = false)
    private LocalDate tanggalPengusulan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pengusulan", nullable = false) 
    private StatusPengusulan statusPengusulan;

    public enum StatusPengusulan {
        MENUNGGU_REVIEW, DITERIMA, DITOLAK
    }

    public Pengusulan() {
        this.tanggalPengusulan = LocalDate.now();
        this.statusPengusulan = StatusPengusulan.MENUNGGU_REVIEW;
    }

    // Getters and Setters tetap sama
    public Long getIdPengusulan() { return idPengusulan; }
    public void setIdPengusulan(Long idPengusulan) { this.idPengusulan = idPengusulan; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getJudulBuku() { return judulBuku; }
    public void setJudulBuku(String judulBuku) { this.judulBuku = judulBuku; }
    public String getPenulis() { return penulis; }
    public void setPenulis(String penulis) { this.penulis = penulis; }
    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public Integer getTahunTerbit() { return tahunTerbit; }
    public void setTahunTerbit(Integer tahunTerbit) { this.tahunTerbit = tahunTerbit; }
    public String getKeteranganPengusulan() { return keteranganPengusulan; }
    public void setKeteranganPengusulan(String keteranganPengusulan) { this.keteranganPengusulan = keteranganPengusulan; }
    public LocalDate getTanggalPengusulan() { return tanggalPengusulan; }
    public void setTanggalPengusulan(LocalDate tanggalPengusulan) { this.tanggalPengusulan = tanggalPengusulan; }
    public StatusPengusulan getStatusPengusulan() { return statusPengusulan; }
    public void setStatusPengusulan(StatusPengusulan statusPengusulan) { this.statusPengusulan = statusPengusulan; }
}
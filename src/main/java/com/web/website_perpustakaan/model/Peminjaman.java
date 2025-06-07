package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "peminjaman")
public class Peminjaman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long peminjamanId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "buku_id", nullable = false)
    private Buku buku;

    @Column(name = "tanggal_peminjaman", nullable = false)
    private LocalDate tanggalPeminjaman;

    @Column(name = "tanggal_pengembalian", nullable = false)
    private LocalDate tanggalPengembalian;

    @Column(name = "tanggal_dikembalikan")
    private LocalDate tanggalDikembalikan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_peminjaman", nullable = false)
    private StatusPeminjaman statusPeminjaman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "denda_id", referencedColumnName = "dendaId")
    private Denda denda;

    public enum StatusPeminjaman {
        DIPINJAM, DIKEMBALIKAN, TERLAMBAT
    }

    // Getters and Setters
    public Long getPeminjamanId() {
        return peminjamanId;
    }

    public void setPeminjamanId(Long peminjamanId) {
        this.peminjamanId = peminjamanId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Buku getBuku() {
        return buku;
    }

    public void setBuku(Buku buku) {
        this.buku = buku;
    }

    public LocalDate getTanggalPeminjaman() {
        return tanggalPeminjaman;
    }

    public void setTanggalPeminjaman(LocalDate tanggalPeminjaman) {
        this.tanggalPeminjaman = tanggalPeminjaman;
    }

    public LocalDate getTanggalPengembalian() {
        return tanggalPengembalian;
    }

    public void setTanggalPengembalian(LocalDate tanggalPengembalian) {
        this.tanggalPengembalian = tanggalPengembalian;
    }

    public LocalDate getTanggalDikembalikan() {
        return tanggalDikembalikan;
    }

    public void setTanggalDikembalikan(LocalDate tanggalDikembalikan) {
        this.tanggalDikembalikan = tanggalDikembalikan;
    }

    public StatusPeminjaman getStatusPeminjaman() {
        return statusPeminjaman;
    }

    public void setStatusPeminjaman(StatusPeminjaman statusPeminjaman) {
        this.statusPeminjaman = statusPeminjaman;
    }

    public Denda getDenda() {
        return denda;
    }

    public void setDenda(Denda denda) {
        this.denda = denda;
    }
}
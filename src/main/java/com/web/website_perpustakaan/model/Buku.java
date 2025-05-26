package com.web.website_perpustakaan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.YearMonth;

@Entity
public class Buku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "buku_id")
    private Long bukuId;

    @NotBlank(message = "Judul tidak boleh kosong")
    @Size(max = 255, message = "Judul maksimal 255 karakter")
    private String judul;

    @Size(max = 13, message = "ISBN maksimal 13 karakter")
    private String isbn;

    @NotBlank(message = "Penulis tidak boleh kosong")
    @Size(max = 255, message = "Penulis maksimal 255 karakter")
    private String penulis;

    @Size(max = 255, message = "Penerbit maksimal 255 karakter")
    private String penerbit;

    @NotBlank(message = "Kategori tidak boleh kosong")
    @Size(max = 50, message = "Kategori maksimal 50 karakter")
    private String kategori;

    @Min(value = 0, message = "Stok tidak boleh negatif")
    private Integer stok;

    @Column(name = "path_cover")
    private String coverPath;

    @Column(name = "path_pdf")
    private String pdfPath;

    @Column(name = "tanggal_terbit")
    private YearMonth tanggalTerbit;

    public Long getBukuId() { return bukuId; }
    public void setBukuId(Long bukuId) { this.bukuId = bukuId; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPenulis() { return penulis; }
    public void setPenulis(String penulis) { this.penulis = penulis; }
    public String getPenerbit() { return penerbit; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public Integer getStok() { return stok; }
    public void setStok(Integer stok) { this.stok = stok; }
    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }
    public YearMonth getTanggalTerbit() { return tanggalTerbit; }
    public void setTanggalTerbit(YearMonth tanggalTerbit) { this.tanggalTerbit = tanggalTerbit; }
}
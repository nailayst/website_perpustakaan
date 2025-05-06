package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "validasi")
public class Validasi implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long validasiId;
    
    @Column(nullable = false)
    private Integer status; // 0 untuk belum validasi, 1 sudah verifikasi
    
    @Column(unique = true)
    private String token;

    // Getters and Setters
    public Long getValidasiId() {
        return validasiId;
    }

    public void setValidasiId(Long validasiId) {
        this.validasiId = validasiId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
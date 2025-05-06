package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "profile")
public class Profile implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;
    
    @Column(nullable = false)
    private String namaLengkap;
    
    private String jenisKelamin;
    
    private String programStudi;
    
    private String fakultas;
    
    private String tahunAngkatan;

    // Getters and Setters
    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public String getProgramStudi() {
        return programStudi;
    }

    public void setProgramStudi(String programStudi) {
        this.programStudi = programStudi;
    }

    public String getFakultas() {
        return fakultas;
    }

    public void setFakultas(String fakultas) {
        this.fakultas = fakultas;
    }

    public String getTahunAngkatan() {
        return tahunAngkatan;
    }

    public void setTahunAngkatan(String tahunAngkatan) {
        this.tahunAngkatan = tahunAngkatan;
    }
}
package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "user")
public class User implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    
    @Column(length = 14, unique = true, nullable = false)
    private String username; // NIM
    
    @Column(nullable = false)
    private String password;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "validasi_id")
    private Validasi validasi;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "luser_id")
    private LevelUser levelUser;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;
    
    @Column(unique = true)
    private String email;

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Validasi getValidasi() {
        return validasi;
    }

    public void setValidasi(Validasi validasi) {
        this.validasi = validasi;
    }

    public LevelUser getLevelUser() {
        return levelUser;
    }

    public void setLevelUser(LevelUser levelUser) {
        this.levelUser = levelUser;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
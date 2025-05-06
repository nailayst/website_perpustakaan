package com.web.website_perpustakaan.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "level_user")
public class LevelUser implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long luserId;
    
    @Column(nullable = false)
    private String levelUser; // admin, member

    // Getters and Setters
    public Long getLuserId() {
        return luserId;
    }

    public void setLuserId(Long luserId) {
        this.luserId = luserId;
    }

    public String getLevelUser() {
        return levelUser;
    }

    public void setLevelUser(String levelUser) {
        this.levelUser = levelUser;
    }
}
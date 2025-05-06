package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.repository.*;
import com.web.website_perpustakaan.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ValidasiRepository validasiRepository;
    
    @Autowired
    private LevelUserRepository levelUserRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public void registerUser(User user, Profile profile) {
        System.out.println("=== DEBUG: Registering user: " + user.getUsername() + ", email: " + user.getEmail());
        if (user.getUsername() == null || user.getUsername().length() != 14 || !user.getUsername().matches("\\d+")) {
            throw new IllegalArgumentException("NIM harus 14 digit angka");
        }
        if (user.getEmail() == null || !user.getEmail().contains("@") || !user.getEmail().contains(".")) {
            throw new IllegalArgumentException("Email tidak valid");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("NIM sudah digunakan");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email sudah digunakan");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmail(user.getEmail()); // Pastikan email disimpan

        Validasi validasi = new Validasi();
        validasi.setStatus(0);
        String token = UUID.randomUUID().toString();
        validasi.setToken(token);
        validasiRepository.save(validasi);
        
        if (user.getLevelUser() == null) {
            LevelUser levelUser = levelUserRepository.findByLevelUser("member")
                .orElseThrow(() -> new IllegalStateException("Level user 'member' tidak ditemukan. Inisialisasi tabel level_user terlebih dahulu."));
            user.setLevelUser(levelUser);
        }
        
        if (profile.getNamaLengkap() == null) {
            profile.setNamaLengkap(user.getUsername()); // Default nama dari username
        }
        profileRepository.save(profile);
        
        user.setValidasi(validasi);
        user.setProfile(profile);
        
        userRepository.save(user);
        
        String verificationLink = "http://localhost:8080/verify?token=" + token;
        String emailContent = "Kepada " + profile.getNamaLengkap() + ",\n\n" +
                             "Selamat datang di KlikPustaka!\n" +
                             "Untuk memverifikasi akun Anda, silakan klik link berikut:\n" +
                             verificationLink + "\n\n" +
                             "Jika Anda tidak mendaftar, abaikan email ini.\n" +
                             "Hubungi kami di support@klikpustaka.com jika ada pertanyaan.\n\n" +
                             "Salam,\nTim KlikPustaka";
        emailService.sendEmail(user.getEmail(), "Verifikasi Akun KlikPustaka", emailContent);
    }
    
    public boolean verifyUser(String token) {
        Validasi validasi = validasiRepository.findByToken(token);
        if (validasi != null && validasi.getStatus() == 0) {
            validasi.setStatus(1);
            validasiRepository.save(validasi);
            System.out.println("=== DEBUG: User verified with token: " + token);
            return true;
        }
        System.out.println("=== DEBUG: Verification failed for token: " + token);
        return false;
    }

    public void updateProfile(Profile profile) {
        profileRepository.save(profile);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
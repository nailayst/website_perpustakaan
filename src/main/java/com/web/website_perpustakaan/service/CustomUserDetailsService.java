package com.web.website_perpustakaan.service;

import com.web.website_perpustakaan.model.User;
import com.web.website_perpustakaan.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String credential) throws UsernameNotFoundException {
        System.out.println("=== DEBUG: Attempting to load user with credential: " + credential);
        User user = null;
        if (credential != null && credential.contains("@")) {
            user = userRepository.findByEmail(credential);
            System.out.println("=== DEBUG: Searching by email: " + credential + ", found: " + (user != null));
        } else if (credential != null && credential.matches("\\d{14}")) {
            user = userRepository.findByUsername(credential);
            System.out.println("=== DEBUG: Searching by username (NIM): " + credential + ", found: " + (user != null));
        } else {
            System.out.println("=== DEBUG: Invalid credential format: " + credential);
            throw new UsernameNotFoundException("Credential format salah");
        }

        if (user == null) {
            System.out.println("=== DEBUG: User not found with credential: " + credential);
            throw new UsernameNotFoundException("User not found with credential: " + credential);
        }

        if (user.getValidasi() == null || user.getValidasi().getStatus() == 0) {
            System.out.println("=== DEBUG: User not verified: " + credential + ", Status: " + (user.getValidasi() != null ? user.getValidasi().getStatus() : "null"));
            throw new UsernameNotFoundException("Akun belum diverifikasi");
        }

        if (user.getLevelUser() == null) {
            System.out.println("=== DEBUG: LevelUser is null for user: " + credential);
            throw new UsernameNotFoundException("Level user tidak ditemukan");
        }

        String role = "ROLE_" + user.getLevelUser().getLevelUser().toUpperCase();
        System.out.println("=== DEBUG: User loaded successfully: " + credential + ", Role: " + role);
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
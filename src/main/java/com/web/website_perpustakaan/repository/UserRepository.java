package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByLevelUser_LevelUser(String levelUser);
    List<User> findByUsernameContainingIgnoreCaseOrProfile_NamaLengkapContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String namaLengkap, String email
    );
    Page<User> findByUsernameContainingIgnoreCaseOrProfile_NamaLengkapContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String namaLengkap, String email, Pageable pageable
    );
}
package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByLevelUser_LevelUser(String levelUser);
}
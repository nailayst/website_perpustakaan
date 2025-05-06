package com.web.website_perpustakaan.repository;

import com.web.website_perpustakaan.model.LevelUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LevelUserRepository extends JpaRepository<LevelUser, Long> {
    Optional<LevelUser> findByLevelUser(String levelUser);
    boolean existsByLevelUser(String levelUser);
}
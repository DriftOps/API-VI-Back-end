package com.xertica.repository;

import com.xertica.entity.UserAnamnesis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAnamnesisRepository extends JpaRepository<UserAnamnesis, Long> {
    Optional<UserAnamnesis> findByUserId(Long userId);
}
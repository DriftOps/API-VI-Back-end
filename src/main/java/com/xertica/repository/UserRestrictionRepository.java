package com.xertica.repository;

import com.xertica.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {
}
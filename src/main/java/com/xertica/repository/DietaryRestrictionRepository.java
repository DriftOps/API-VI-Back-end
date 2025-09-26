package com.xertica.repository;

import com.xertica.entity.DietaryRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DietaryRestrictionRepository extends JpaRepository<DietaryRestriction, Long> {
    Optional<DietaryRestriction> findByName(String name);
}
package com.xertica.repository;

import com.xertica.entity.DietaryPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DietaryPreferenceRepository extends JpaRepository<DietaryPreference, Long> {
    Optional<DietaryPreference> findByName(String name);
}
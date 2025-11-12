package com.xertica.repository;

import com.xertica.entity.Diet;
import com.xertica.entity.enums.DietStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface DietRepository extends JpaRepository<Diet, Long> {
    
    Optional<Diet> findByUserIdAndStatus(Long userId, DietStatus status);

    @Query("SELECT d FROM Diet d LEFT JOIN FETCH d.dailyTargets dt WHERE d.user.id = :userId AND d.status = :status ORDER BY dt.targetDate ASC")
    Optional<Diet> findByUserIdAndStatusFetchDailyTargets(Long userId, DietStatus status);

    List<Diet> findAllByStatus(DietStatus status);
}
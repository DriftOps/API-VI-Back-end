package com.xertica.repository;

import com.xertica.entity.DietDailyTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietDailyTargetRepository extends JpaRepository<DietDailyTarget, Long> {

    Optional<DietDailyTarget> findByDietIdAndTargetDate(Long dietId, LocalDate targetDate);

    // Ordenado por data, essencial para a IA
    List<DietDailyTarget> findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(Long dietId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(m.calories), 0) FROM Meal m WHERE m.user.id = :userId AND DATE(m.createdAt) = :date")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
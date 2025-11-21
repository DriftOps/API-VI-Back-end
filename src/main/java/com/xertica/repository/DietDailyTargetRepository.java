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

    List<DietDailyTarget> findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(Long dietId, LocalDate startDate, LocalDate endDate);

    // Soma calorias do dia
    @Query("SELECT COALESCE(SUM(m.calories), 0) FROM Meal m WHERE m.user.id = :userId AND m.mealDate = :date")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Soma Proteínas
    @Query("SELECT COALESCE(SUM(m.protein), 0) FROM Meal m WHERE m.user.id = :userId AND m.mealDate = :date")
    Double sumProteinByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Soma Carbos
    @Query("SELECT COALESCE(SUM(m.carbs), 0) FROM Meal m WHERE m.user.id = :userId AND m.mealDate = :date")
    Double sumCarbsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Soma Gorduras
    @Query("SELECT COALESCE(SUM(m.fat), 0) FROM Meal m WHERE m.user.id = :userId AND m.mealDate = :date")
    Double sumFatByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
package com.xertica.repository;

import com.xertica.entity.Meal;
import com.xertica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByUser(User user);

    List<Meal> findByUserAndMealDate(User user, LocalDate mealDate);

    @Query("SELECT COALESCE(SUM(m.calories), 0) FROM Meal m WHERE m.user.id = :userId AND DATE(m.createdAt) = :date")
    Integer sumCaloriesByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
package com.xertica.repository;

import com.xertica.entity.Meal;
import com.xertica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByUser(User user);

    List<Meal> findByUserAndMealDate(User user, LocalDate mealDate);
}

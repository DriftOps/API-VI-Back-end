package com.xertica.service;

import com.xertica.entity.Meal;
import com.xertica.entity.User;
import com.xertica.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealService {
    private final MealRepository mealRepository;

    public List<Meal> getMeals(User user) {
        return mealRepository.findByUser(user);
    }

    public Meal saveMeal(Meal meal, User user) {
        meal.setUser(user);
        meal.setCreatedAt(LocalDateTime.now());
        return mealRepository.save(meal);
    }

    public void deleteMeal(Long id, User user) {
        Meal meal = mealRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Refeição não encontrada."));
        mealRepository.delete(meal);
    }
}

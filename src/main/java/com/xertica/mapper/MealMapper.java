package com.xertica.mapper;

import com.xertica.dto.MealDTO;
import com.xertica.entity.Meal;

public class MealMapper {
    public static MealDTO toMealDTO(Meal meal) {
        MealDTO dto = new MealDTO();
        dto.setId(meal.getId());
        dto.setType(meal.getType());
        dto.setDescription(meal.getDescription());
        dto.setCalories(meal.getCalories());
        dto.setProtein(meal.getProtein());
        dto.setCarbs(meal.getCarbs());
        dto.setFat(meal.getFat());
        dto.setCreatedAt(meal.getCreatedAt());
        dto.setMealDate(meal.getMealDate());
        return dto;
    }

    public static Meal toMealEntity(MealDTO dto) {
        Meal meal = new Meal();
        meal.setType(dto.getType());
        meal.setDescription(dto.getDescription());
        meal.setCalories(dto.getCalories());
        meal.setProtein(dto.getProtein());
        meal.setCarbs(dto.getCarbs());
        meal.setFat(dto.getFat());

        return meal;
    }
}
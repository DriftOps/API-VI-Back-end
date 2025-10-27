package com.xertica.controller;

import com.xertica.entity.Meal;
import com.xertica.entity.User;
import com.xertica.service.MealService;
import com.xertica.service.UserService;
import com.xertica.dto.MealDTO; 
import com.xertica.mapper.MealMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final UserService userService;

    @GetMapping
    public List<MealDTO> getUserMeals(@RequestHeader("Authorization") String token,
                                   @RequestParam(required = false) String date) {
        User user = userService.getUserFromToken(token);
        List<Meal> meals;

        if (date != null) {
            LocalDate localDate = LocalDate.parse(date);
            meals = mealService.getMealsByDate(user, localDate);
        } else {
            meals = mealService.getMeals(user);
        }

        return meals.stream()
                    .map(MealMapper::toMealDTO)
                    .collect(Collectors.toList());
    }



@PostMapping
public MealDTO addMeal(@RequestHeader("Authorization") String token, @RequestBody MealDTO mealDTO) {
    User user = userService.getUserFromToken(token);

    Meal meal = MealMapper.toMealEntity(mealDTO);
    Meal savedMeal = mealService.saveMeal(meal, user);
    // Converta a entidade para DTO antes de retornar
    return MealMapper.toMealDTO(savedMeal);
}

    @DeleteMapping("/{id}")
    public void deleteMeal(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        User user = userService.getUserFromToken(token);
        mealService.deleteMeal(id, user);
    }

    
}

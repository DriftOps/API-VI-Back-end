package com.xertica.controller;

import com.xertica.entity.Meal;
import com.xertica.entity.User;
import com.xertica.service.MealService;
import com.xertica.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final UserService userService;

    @GetMapping
    public List<Meal> getUserMeals(@RequestHeader("Authorization") String token) {
        User user = userService.getUserFromToken(token);
        return mealService.getMeals(user);
    }

    @PostMapping
    public Meal addMeal(@RequestHeader("Authorization") String token, @RequestBody Meal meal) {
        User user = userService.getUserFromToken(token);
        return mealService.saveMeal(meal, user);
    }

    @DeleteMapping("/{id}")
    public void deleteMeal(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        User user = userService.getUserFromToken(token);
        mealService.deleteMeal(id, user);
    }
}

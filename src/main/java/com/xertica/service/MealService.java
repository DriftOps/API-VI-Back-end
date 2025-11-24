package com.xertica.service;

import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.entity.Meal;
import com.xertica.entity.User;
import com.xertica.entity.enums.DietStatus;
import com.xertica.repository.DietDailyTargetRepository;
import com.xertica.repository.DietRepository;
import com.xertica.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final DietRepository dietRepository;
    private final DietDailyTargetRepository dietDailyTargetRepository;
    
    //  1. INJEÇÃO OBRIGATÓRIA DO JOB
    private final DietBalanceJob dietBalanceJob; 

    public List<Meal> getMeals(User user) {
        return mealRepository.findByUser(user);
    }

    public List<Meal> getMealsByDate(User user, LocalDate date) {
        return mealRepository.findByUserAndMealDate(user, date);
    }

    @Transactional
    public Meal saveMeal(Meal meal, User user) {
        meal.setUser(user);
        
        if (meal.getMealDate() == null) {
            meal.setMealDate(LocalDate.now());
        }
        if (meal.getCreatedAt() == null) {
            meal.setCreatedAt(LocalDateTime.now());
        }

        Meal savedMeal = mealRepository.save(meal);

        //  2. CHAMA A ATUALIZAÇÃO
        updateDietTotals(user.getId(), savedMeal.getMealDate());

        return savedMeal;
    }

    @Transactional
    public void deleteMeal(Long id, User user) {
        Meal meal = mealRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Refeição não encontrada."));
        
        LocalDate mealDate = meal.getMealDate();
        mealRepository.delete(meal);

        updateDietTotals(user.getId(), mealDate);
    }

    private void updateDietTotals(Long userId, LocalDate date) {
        Optional<Diet> activeDietOpt = dietRepository.findByUserIdAndStatus(userId, DietStatus.ACTIVE);

        if (activeDietOpt.isPresent()) {
            Diet activeDiet = activeDietOpt.get();

            if (!date.isBefore(activeDiet.getStartDate()) && !date.isAfter(activeDiet.getEndDate())) {
                DietDailyTarget target = dietDailyTargetRepository.findByDietIdAndTargetDate(activeDiet.getId(), date)
                        .orElse(null);

                if (target != null) {
                    Integer totalCalories = dietDailyTargetRepository.sumCaloriesByUserIdAndDate(userId, date);
                    Double totalProtein = dietDailyTargetRepository.sumProteinByUserIdAndDate(userId, date);
                    Double totalCarbs = dietDailyTargetRepository.sumCarbsByUserIdAndDate(userId, date);
                    Double totalFat = dietDailyTargetRepository.sumFatByUserIdAndDate(userId, date);

                    target.setConsumedCalories(totalCalories != null ? totalCalories : 0);
                    target.setConsumedProteinG(BigDecimal.valueOf(totalProtein != null ? totalProtein : 0.0));
                    target.setConsumedCarbsG(BigDecimal.valueOf(totalCarbs != null ? totalCarbs : 0.0));
                    target.setConsumedFatsG(BigDecimal.valueOf(totalFat != null ? totalFat : 0.0));

                    dietDailyTargetRepository.save(target);

                    //  3. O GATILHO REATIVO:
                    // Se consumiu mais que a meta, chama o rebalanceamento IMEDIATAMENTE
                    if (target.getConsumedCalories() > target.getAdjustedCalories()) {
                        System.out.println("🚨 ALERTA: Consumo (" + target.getConsumedCalories() + 
                                           ") > Meta (" + target.getAdjustedCalories() + 
                                           "). Acionando IA para rebalancear...");
                        
                        dietBalanceJob.rebalanceDiet(activeDiet, date);
                    }
                }
            }
        }
    }
}
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

    public List<Meal> getMeals(User user) {
        return mealRepository.findByUser(user);
    }

    public List<Meal> getMealsByDate(User user, LocalDate date) {
        return mealRepository.findByUserAndMealDate(user, date);
    }

    @Transactional
    public Meal saveMeal(Meal meal, User user) {
        meal.setUser(user);
        
        // Garante datas
        if (meal.getMealDate() == null) {
            meal.setMealDate(LocalDate.now());
        }
        if (meal.getCreatedAt() == null) {
            meal.setCreatedAt(LocalDateTime.now());
        }

        Meal savedMeal = mealRepository.save(meal);

        // 🔥 ATUALIZAÇÃO AUTOMÁTICA DA DIETA
        updateDietTotals(user.getId(), savedMeal.getMealDate());

        return savedMeal;
    }

    @Transactional
    public void deleteMeal(Long id, User user) {
        Meal meal = mealRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Refeição não encontrada."));
        
        // Guarda a data antes de deletar para saber qual dia recalcular
        LocalDate mealDate = meal.getMealDate();
        
        mealRepository.delete(meal);

        // 🔥 RECALCULA O DIA (agora sem essa refeição)
        updateDietTotals(user.getId(), mealDate);
    }

    /**
     * Método auxiliar que busca a dieta ativa e recalcula
     * o total consumido no dia específico, salvando na tabela de metas.
     */
    private void updateDietTotals(Long userId, LocalDate date) {
        // 1. Verifica se existe dieta ativa
        Optional<Diet> activeDietOpt = dietRepository.findByUserIdAndStatus(userId, DietStatus.ACTIVE);

        if (activeDietOpt.isPresent()) {
            Diet activeDiet = activeDietOpt.get();

            // 2. Verifica se a data da refeição afeta a dieta atual
            if (!date.isBefore(activeDiet.getStartDate()) && !date.isAfter(activeDiet.getEndDate())) {

                // 3. Busca ou ignora se não houver meta para o dia (embora devesse haver)
                DietDailyTarget target = dietDailyTargetRepository.findByDietIdAndTargetDate(activeDiet.getId(), date)
                        .orElse(null);

                if (target != null) {
                    // 4. Executa as somas no banco de dados (Single Source of Truth)
                    Integer totalCalories = dietDailyTargetRepository.sumCaloriesByUserIdAndDate(userId, date);
                    Double totalProtein = dietDailyTargetRepository.sumProteinByUserIdAndDate(userId, date);
                    Double totalCarbs = dietDailyTargetRepository.sumCarbsByUserIdAndDate(userId, date);
                    Double totalFat = dietDailyTargetRepository.sumFatByUserIdAndDate(userId, date);

                    // 5. Atualiza e salva a meta diária
                    target.setConsumedCalories(totalCalories != null ? totalCalories : 0);
                    
                    // Converte Double para BigDecimal conforme sua Entidade
                    target.setConsumedProteinG(BigDecimal.valueOf(totalProtein != null ? totalProtein : 0.0));
                    target.setConsumedCarbsG(BigDecimal.valueOf(totalCarbs != null ? totalCarbs : 0.0));
                    target.setConsumedFatsG(BigDecimal.valueOf(totalFat != null ? totalFat : 0.0));

                    dietDailyTargetRepository.save(target);
                }
            }
        }
    }
}
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
import lombok.extern.slf4j.Slf4j; // Recomendado para logs, se usar Lombok
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j // Opcional: Se você usar Slf4j para logs
public class MealService {

    private final MealRepository mealRepository;
    private final DietRepository dietRepository;
    private final DietDailyTargetRepository dietDailyTargetRepository;
    
    // Injetamos o Job que fala com a IA
    private final DietBalanceJob dietBalanceJob; 

    // Define uma margem de tolerância (em calorias) para não acionar a IA por 1kcal
    private static final int CALORIE_TOLERANCE_THRESHOLD = 50;

    public List<Meal> getMeals(User user) {
        return mealRepository.findByUser(user);
    }

    public List<Meal> getMealsByDate(User user, LocalDate date) {
        return mealRepository.findByUserAndMealDate(user, date);
    }

    @Transactional
    public Meal saveMeal(Meal meal, User user) {
        meal.setUser(user);
        
        // Garante datas padrões se não vierem do front
        if (meal.getMealDate() == null) {
            meal.setMealDate(LocalDate.now());
        }
        if (meal.getCreatedAt() == null) {
            meal.setCreatedAt(LocalDateTime.now());
        }

        Meal savedMeal = mealRepository.save(meal);

        // 🔥 Atualiza totais e verifica necessidade de rebalanceamento
        updateDietTotals(user.getId(), savedMeal.getMealDate());

        return savedMeal;
    }

    @Transactional
    public void deleteMeal(Long id, User user) {
        Meal meal = mealRepository.findById(id)
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new RuntimeException("Refeição não encontrada ou acesso negado."));
        
        LocalDate mealDate = meal.getMealDate();
        
        mealRepository.delete(meal);

        // 🔥 Recalcula o dia após a remoção
        updateDietTotals(user.getId(), mealDate);
    }

    /**
     * Método auxiliar que busca a dieta ativa, recalcula o total consumido 
     * no dia específico e dispara a IA se houver estouro significativo.
     */
    private void updateDietTotals(Long userId, LocalDate date) {
        Optional<Diet> activeDietOpt = dietRepository.findByUserIdAndStatus(userId, DietStatus.ACTIVE);

        if (activeDietOpt.isPresent()) {
            Diet activeDiet = activeDietOpt.get();

            // Verifica se a data da refeição está dentro da vigência da dieta
            if (!date.isBefore(activeDiet.getStartDate()) && !date.isAfter(activeDiet.getEndDate())) {

                DietDailyTarget target = dietDailyTargetRepository.findByDietIdAndTargetDate(activeDiet.getId(), date)
                        .orElse(null);

                if (target != null) {
                    // 1. Busca os somatórios atualizados do banco
                    Integer totalCalories = dietDailyTargetRepository.sumCaloriesByUserIdAndDate(userId, date);
                    Double totalProtein = dietDailyTargetRepository.sumProteinByUserIdAndDate(userId, date);
                    Double totalCarbs = dietDailyTargetRepository.sumCarbsByUserIdAndDate(userId, date);
                    Double totalFat = dietDailyTargetRepository.sumFatByUserIdAndDate(userId, date);

                    // Trata nulls (caso o usuário tenha apagado todas as refeições do dia)
                    int currentCalories = totalCalories != null ? totalCalories : 0;
                    
                    // 2. Atualiza a meta diária com o consumido real
                    target.setConsumedCalories(currentCalories);
                    target.setConsumedProteinG(BigDecimal.valueOf(totalProtein != null ? totalProtein : 0.0));
                    target.setConsumedCarbsG(BigDecimal.valueOf(totalCarbs != null ? totalCarbs : 0.0));
                    target.setConsumedFatsG(BigDecimal.valueOf(totalFat != null ? totalFat : 0.0));

                    dietDailyTargetRepository.save(target);

                    // 3. VERIFICAÇÃO DE ESTOURO E DISPARO DA IA (TRIGGER)
                    // Regra: Deve ser HOJE (não rebalanceamos o passado) e deve exceder a meta + tolerância
                    if (date.isEqual(LocalDate.now())) {
                        
                        int limitWithTolerance = target.getAdjustedCalories() + CALORIE_TOLERANCE_THRESHOLD;

                        if (currentCalories > limitWithTolerance) {
                            System.out.println("Estouro de calorias detectado (" + currentCalories + " > " + limitWithTolerance + "). Acionando IA...");
                            try {
                                // Chama o Job. Se o método no Job estiver anotado com @Async,
                                // isso retorna instantaneamente e não trava o saveMeal.
                                dietBalanceJob.balanceDietForUser(userId);
                            } catch (Exception e) {
                                // Falha silenciosa para não impedir o usuário de salvar a refeição
                                System.err.println("Aviso: Falha ao disparar rebalanceamento automático: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
}
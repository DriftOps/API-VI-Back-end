package com.xertica.service;

import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.entity.enums.DietStatus;
import com.xertica.repository.DietRepository;
import com.xertica.repository.DietDailyTargetRepository;
import com.xertica.repository.MealRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DietBalanceJob {

    // --- DTOs para comunicação com a IA ---
    @Getter @Setter static class AiBalanceRequest {
        private int base_calories;
        private int safe_metabolic_floor;
        private List<DailyData> recent_days;
    }
    @Getter @Setter static class DailyData {
        private int target_calories;
        private int consumed_calories;
    }
    @Getter @Setter static class AiBalanceResponse {
        private int new_adjusted_calories;
        private String ai_rationale;
    }
    // --- Fim dos DTOs ---

    private final DietRepository dietRepository;
    private final DietDailyTargetRepository ddtRepository;
    private final MealRepository mealRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url}") // Defina isso no seu application.properties
    private String AI_SERVICE_URL; // ex: http://localhost:5000/ai/balance-diet

    public DietBalanceJob(DietRepository dietRepository, DietDailyTargetRepository ddtRepository, MealRepository mealRepository) {
        this.dietRepository = dietRepository;
        this.ddtRepository = ddtRepository;
        this.mealRepository = mealRepository;
        this.restTemplate = new RestTemplate();
    }

    // Roda todo dia às 02:00 da manhã
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runDailyBalance() {
        List<Diet> activeDiets = dietRepository.findAllByStatus(DietStatus.ACTIVE);

        for (Diet diet : activeDiets) {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // 1. Atualizar o consumo de "ontem"
            Integer consumedYesterday = mealRepository.sumCaloriesByUserIdAndDate(diet.getUser().getId(), yesterday);
            DietDailyTarget yesterdayTarget = ddtRepository.findByDietIdAndTargetDate(diet.getId(), yesterday)
                .orElse(null);
            
            if (yesterdayTarget != null) {
                yesterdayTarget.setConsumedCalories(consumedYesterday != null ? consumedYesterday : 0);
                ddtRepository.save(yesterdayTarget);
            }

            // 2. Preparar dados para a IA (últimos 7 dias)
            LocalDate startDate = yesterday.minusDays(6);
            List<DietDailyTarget> recentTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(diet.getId(), startDate, yesterday);
            
            AiBalanceRequest request = new AiBalanceRequest();
            request.base_calories = diet.getBaseDailyCalories();
            request.safe_metabolic_floor = diet.getSafeMetabolicFloor();
            request.recent_days = recentTargets.stream()
                .map(t -> {
                    DailyData dd = new DailyData();
                    dd.target_calories = t.getAdjustedCalories();
                    dd.consumed_calories = t.getConsumedCalories();
                    return dd;
                }).collect(Collectors.toList());

            // 3. Chamar a IA
            try {
                AiBalanceResponse response = restTemplate.postForObject(AI_SERVICE_URL, request, AiBalanceResponse.class);

                // 4. Aplicar a nova meta da IA
                if (response != null && response.new_adjusted_calories > 0) {
                    applyNewTargetToFuture(diet, response.new_adjusted_calories);
                    diet.setAiRationale(response.ai_rationale);
                    dietRepository.save(diet);
                }
            } catch (Exception e) {
                // Logar erro, mas não parar o job
                System.err.println("Erro ao rebalancear dieta " + diet.getId() + ": " + e.getMessage());
            }
        }
    }

    private void applyNewTargetToFuture(Diet diet, int newCalories) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(6); // Ajusta a próxima semana (7 dias)

        List<DietDailyTarget> futureTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(
            diet.getId(), 
            today, 
            endDate.isAfter(diet.getEndDate()) ? diet.getEndDate() : endDate
        );

        for (DietDailyTarget target : futureTargets) {
            target.setAdjustedCalories(newCalories);
        }
        ddtRepository.saveAll(futureTargets);
    }
}
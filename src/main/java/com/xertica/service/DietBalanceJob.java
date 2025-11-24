package com.xertica.service;

import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.repository.DietDailyTargetRepository;
import com.xertica.repository.DietRepository;
import com.xertica.repository.MealRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DietBalanceJob {

    // --- DTOs Atualizados ---
    @Getter @Setter static class AiBalanceRequest {
        private int base_calories;
        private int safe_metabolic_floor;
        private List<DailyData> recent_days;
    }
    @Getter @Setter static class DailyData {
        private int target_calories;
        private int consumed_calories;
    }
    
    // MUDANÇA AQUI: Recebe lista de inteiros agora
    @Getter @Setter static class AiBalanceResponse {
        private List<Integer> next_days_targets; 
        private String ai_rationale;
    }
    // --- Fim dos DTOs ---

    private final DietRepository dietRepository;
    private final DietDailyTargetRepository ddtRepository;
    private final MealRepository mealRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.service.url}") 
    private String AI_SERVICE_URL;

    public DietBalanceJob(DietRepository dietRepository, DietDailyTargetRepository ddtRepository, MealRepository mealRepository) {
        this.dietRepository = dietRepository;
        this.ddtRepository = ddtRepository;
        this.mealRepository = mealRepository;
        this.restTemplate = new RestTemplate();
    }

    // Método reativo (chamado pelo MealService quando estoura meta)
    @Transactional
    public void rebalanceDiet(Diet diet, LocalDate referenceDate) {
        System.out.println(">>> INICIANDO REBALANCEAMENTO para dieta " + diet.getId());
        try {
            // 1. Garante dados atualizados no banco
            Integer consumedOnReferenceDate = mealRepository.sumCaloriesByUserIdAndDate(diet.getUser().getId(), referenceDate);
            DietDailyTarget referenceTarget = ddtRepository.findByDietIdAndTargetDate(diet.getId(), referenceDate).orElse(null);

            if (referenceTarget != null) {
                referenceTarget.setConsumedCalories(consumedOnReferenceDate != null ? consumedOnReferenceDate : 0);
                ddtRepository.save(referenceTarget);
            }

            // 2. Prepara dados para IA
            LocalDate startDate = referenceDate.minusDays(6);
            List<DietDailyTarget> recentTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(diet.getId(), startDate, referenceDate);
            
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

            // 3. Chama a IA
            System.out.println(">>> Enviando request para IA: " + request.getRecent_days().size() + " dias.");

            AiBalanceResponse response = restTemplate.postForObject(AI_SERVICE_URL, request, AiBalanceResponse.class);

            System.out.println(">>> Resposta da IA recebida. Plano: " + response.getNext_days_targets());

            // 4. Aplica o PLANO COMPLEXO da IA
            if (response != null && response.next_days_targets != null && !response.next_days_targets.isEmpty()) {
                
                // Se referência é HOJE, começamos a aplicar AMANHÃ
                LocalDate startApplyingDate = referenceDate.plusDays(1);
                
                applyAiPlanToFuture(diet, response.next_days_targets, startApplyingDate, response.ai_rationale);
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO CRÍTICO NO REBALANCEAMENTO: " + e.getMessage());            e.printStackTrace();
        }
    }

    private void applyAiPlanToFuture(Diet diet, List<Integer> aiTargetsPlan, LocalDate startFromDate, String rationale) {
        // Busca os próximos N dias (baseado no tamanho do plano da IA)
        int planSize = aiTargetsPlan.size();
        LocalDate endDate = startFromDate.plusDays(planSize - 1); 

        List<DietDailyTarget> futureTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(
                diet.getId(),
                startFromDate,
                endDate.isAfter(diet.getEndDate()) ? diet.getEndDate() : endDate
        );

        boolean changed = false;
        
        // Itera sobre os dias encontrados no banco e aplica o valor correspondente do plano da IA
        for (int i = 0; i < futureTargets.size(); i++) {
            DietDailyTarget target = futureTargets.get(i);
            
            // Proteção para não estourar o índice se o banco tiver menos dias que o plano
            if (i < aiTargetsPlan.size()) {
                Integer newTargetValue = aiTargetsPlan.get(i);
                if (target.getAdjustedCalories() != newTargetValue) {
                    target.setAdjustedCalories(newTargetValue);
                    changed = true;
                }
            }
        }
        
        if (changed) {
            ddtRepository.saveAll(futureTargets);
            if (rationale != null && !rationale.isEmpty()) {
                diet.setAiRationale(rationale);
                dietRepository.save(diet);
            }
        }
    }
}
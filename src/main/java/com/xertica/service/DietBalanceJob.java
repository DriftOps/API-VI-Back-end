package com.xertica.service;

import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.entity.enums.DietStatus;
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
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Async;

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

    @Value("${ai.service.url}")
    private String AI_SERVICE_URL;

    public DietBalanceJob(DietRepository dietRepository, DietDailyTargetRepository ddtRepository, MealRepository mealRepository) {
        this.dietRepository = dietRepository;
        this.ddtRepository = ddtRepository;
        this.mealRepository = mealRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Método acionado sob demanda (Trigger) quando o usuário excede a meta.
     * Não roda mais via @Scheduled.
     */
    @Transactional
    @Async 
    public void balanceDietForUser(Long userId) {
        Optional<Diet> dietOpt = dietRepository.findByUserIdAndStatus(userId, DietStatus.ACTIVE);
        
        if (dietOpt.isPresent()) {
            Diet diet = dietOpt.get();
            LocalDate today = LocalDate.now();

            // 1. Preparar dados para a IA 
            // Buscamos os últimos 6 dias + HOJE (pois o estouro é hoje)
            LocalDate startDate = today.minusDays(6);
            List<DietDailyTarget> recentTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(diet.getId(), startDate, today);
            
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

            // 2. Chamar a IA
            try {
                System.out.println("Chamando IA para rebalancear dieta do usuário " + userId + "...");
                AiBalanceResponse response = restTemplate.postForObject(AI_SERVICE_URL, request, AiBalanceResponse.class);

                if (response != null && response.new_adjusted_calories > 0) {
                    
                    // --- TRAVA DE SEGURANÇA METABÓLICA (HARD CLAMP) ---
                    // A IA sugere, mas o Java dita as regras finais.
                    // Se a IA sugerir 1000, mas o piso for 1400, usamos 1400.
                    int finalCalories = Math.max(response.new_adjusted_calories, diet.getSafeMetabolicFloor());
                    
                    // Log de auditoria interna se a IA violou o piso
                    if (finalCalories > response.new_adjusted_calories) {
                         System.out.println("SAFETY TRIGGER: IA tentou cruzar o piso metabólico. Ajustado para o limite seguro: " + finalCalories);
                    }

                    applyNewTargetToFuture(diet, finalCalories);
                    
                    // Adiciona nota automática ao racional se houver ajuste de segurança
                    String rationale = response.ai_rationale;
                    if (finalCalories != response.new_adjusted_calories) {
                        rationale += " [Nota do Sistema: O valor foi ajustado para respeitar seu metabolismo basal de segurança.]";
                    }
                    
                    diet.setAiRationale(rationale);
                    dietRepository.save(diet);
                }
            } catch (Exception e) {
                System.err.println("Erro na comunicação com IA: " + e.getMessage());
            }
        }
    }

    private void applyNewTargetToFuture(Diet diet, int newCalories) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate endDate = tomorrow.plusDays(6); // Ajusta a próxima semana (7 dias a partir de amanhã)

        List<DietDailyTarget> futureTargets = ddtRepository.findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(
            diet.getId(), 
            tomorrow, 
            endDate.isAfter(diet.getEndDate()) ? diet.getEndDate() : endDate
        );

        for (DietDailyTarget target : futureTargets) {
            target.setAdjustedCalories(newCalories);
        }
        ddtRepository.saveAll(futureTargets);
    }
}
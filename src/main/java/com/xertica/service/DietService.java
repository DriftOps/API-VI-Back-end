package com.xertica.service;

import com.xertica.dto.CreateDietRequestDTO;
import com.xertica.dto.DietViewDTO;
import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.entity.User;
import com.xertica.entity.enums.DietStatus;
import com.xertica.mapper.DietMapper;
import com.xertica.repository.DietRepository;
import com.xertica.repository.DietDailyTargetRepository;
import com.xertica.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Service
public class DietService {

    private final DietRepository dietRepository;
    private final DietDailyTargetRepository dietDailyTargetRepository;
    private final UserRepository userRepository;
    private final DietMapper dietMapper;
    // Assumindo que você tem um serviço que calcula a TMB (Piso Seguro)
    // private final UserAnamnesisService userAnamnesisService; 

    public DietService(DietRepository dietRepository, 
                         DietDailyTargetRepository ddtRepository, 
                         UserRepository userRepository, 
                         DietMapper dietMapper) {
        this.dietRepository = dietRepository;
        this.dietDailyTargetRepository = ddtRepository;
        this.userRepository = userRepository;
        this.dietMapper = dietMapper;
    }

    @Transactional
    public DietViewDTO createDiet(CreateDietRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        dietRepository.findByUserIdAndStatus(user.getId(), DietStatus.ACTIVE)
            .ifPresent(oldDiet -> oldDiet.setStatus(DietStatus.CANCELLED));

        Diet diet = new Diet();
        diet.setUser(user);
        diet.setTitle(request.getTitle());
        diet.setStartDate(LocalDate.now());
        diet.setEndDate(request.getEndDate());
        
        if (user.getWeight() != null) {
            diet.setInitialWeight(BigDecimal.valueOf(user.getWeight()));
        } else {
            diet.setInitialWeight(BigDecimal.ZERO); 
        }

        diet.setTargetWeight(request.getTargetWeight());
        diet.setBaseDailyCalories(request.getBaseDailyCalories());
        
        // --- NOVO: Setando Macros e Racional ---
        diet.setBaseDailyProteinG(request.getBaseDailyProteinG());
        diet.setBaseDailyCarbsG(request.getBaseDailyCarbsG());
        diet.setBaseDailyFatsG(request.getBaseDailyFatsG());
        diet.setAiRationale(request.getAiRationale()); // Salva o texto da IA
        // ---------------------------------------

        diet.setSafeMetabolicFloor(request.getSafeMetabolicFloor());
        diet.setStatus(DietStatus.ACTIVE);

        Diet savedDiet = dietRepository.save(diet);

        List<DietDailyTarget> dailyTargets = new ArrayList<>();
        for (LocalDate date = diet.getStartDate(); !date.isAfter(diet.getEndDate()); date = date.plusDays(1)) {
            DietDailyTarget dailyTarget = new DietDailyTarget();
            dailyTarget.setDiet(savedDiet);
            dailyTarget.setTargetDate(date);
            dailyTarget.setAdjustedCalories(request.getBaseDailyCalories());
            
            dailyTarget.setAdjustedProteinG(request.getBaseDailyProteinG());
            dailyTarget.setAdjustedCarbsG(request.getBaseDailyCarbsG());
            dailyTarget.setAdjustedFatsG(request.getBaseDailyFatsG());
            // ------------------------------------------
            
            dailyTargets.add(dailyTarget);
        }
        
        dietDailyTargetRepository.saveAll(dailyTargets);
        

        return getActiveDietForUser(user.getId()); 
    }

    @Transactional(readOnly = true)
    public DietViewDTO getActiveDietForUser(Long userId) {
        Diet diet = dietRepository.findByUserIdAndStatusFetchDailyTargets(userId, DietStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Nenhuma dieta ativa encontrada"));
        
        return dietMapper.dietToDietViewDTO(diet);
    }
}
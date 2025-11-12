package com.xertica.service;

import com.xertica.dto.CreateDietRequestDTO;
import com.xertica.dto.DietViewDTO;
import com.xertica.dto.UpdateDailyTargetDTO; 
import com.xertica.entity.Diet;
import com.xertica.entity.DietDailyTarget;
import com.xertica.dto.DietDailyTargetDTO; 
import com.xertica.entity.User;
import com.xertica.entity.enums.DietStatus;
import com.xertica.mapper.DietMapper;
import com.xertica.repository.DietRepository;
import com.xertica.repository.DietDailyTargetRepository;
import com.xertica.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException; 

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // (NOVO)
import java.math.BigDecimal;

@Service
public class DietService {

    private final DietRepository dietRepository;
    private final DietDailyTargetRepository dietDailyTargetRepository;
    private final UserRepository userRepository;
    private final DietMapper dietMapper;

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

        // (Opcional) Cancela qualquer dieta ativa antiga
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
        diet.setSafeMetabolicFloor(request.getSafeMetabolicFloor());
        diet.setStatus(DietStatus.ACTIVE);

        Diet savedDiet = dietRepository.save(diet);

        // ... (criação das metas diárias) ...
        List<DietDailyTarget> dailyTargets = new ArrayList<>();
        for (LocalDate date = diet.getStartDate(); !date.isAfter(diet.getEndDate()); date = date.plusDays(1)) {
            DietDailyTarget dailyTarget = new DietDailyTarget();
            dailyTarget.setDiet(savedDiet);
            dailyTarget.setTargetDate(date);
            dailyTarget.setAdjustedCalories(request.getBaseDailyCalories());
            dailyTargets.add(dailyTarget);
        }
        dietDailyTargetRepository.saveAll(dailyTargets);
        
        // Mapeia o DTO da entidade salva
        return dietMapper.dietToDietViewDTO(savedDiet);
    }


    @Transactional(readOnly = true)
    public Optional<DietViewDTO> getActiveDietForUser(Long userId) {
        // Altera o retorno de DietViewDTO para Optional<DietViewDTO>
        
        // Em vez de '.orElseThrow()', usamos '.map()'
        return dietRepository.findByUserIdAndStatusFetchDailyTargets(userId, DietStatus.ACTIVE)
                .map(dietMapper::dietToDietViewDTO); 
        // Se não encontrar, ele retorna um Optional.empty()
    }

    @Transactional
    public DietDailyTargetDTO updateDailyTarget(Long dailyTargetId, UpdateDailyTargetDTO dto, Long userId) {
        DietDailyTarget target = dietDailyTargetRepository.findById(dailyTargetId)
                .orElseThrow(() -> new RuntimeException("Meta diária não encontrada"));
        
        // Verificação de segurança: O usuário logado é dono desta meta?
        if (!target.getDiet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Você não tem permissão para editar esta meta.");
        }

        // Não permitir edição de datas passadas
        if (target.getTargetDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível editar metas de datas passadas.");
        }
        
        target.setAdjustedCalories(dto.getAdjustedCalories());
        // (Opcional) Adicionar lógica para recalcular macros se necessário
        
        DietDailyTarget savedTarget = dietDailyTargetRepository.save(target);
        return dietMapper.dietDailyTargetToDTO(savedTarget);
    }

    @Transactional
    public void cancelDiet(Long dietId, Long userId) {
        Diet diet = dietRepository.findById(dietId)
                .orElseThrow(() -> new RuntimeException("Dieta não encontrada"));
        
        // Verificação de segurança
        if (!diet.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Você não tem permissão para cancelar esta dieta.");
        }
        
        diet.setStatus(DietStatus.CANCELLED);
        dietRepository.save(diet);
    }
}
package com.xertica.service;

import com.xertica.entity.User;
import com.xertica.entity.UserAnamnesis;
import com.xertica.repository.UserAnamnesisRepository;
import com.xertica.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAnamnesisService {

    private final UserAnamnesisRepository anamnesisRepository;
    private final UserRepository userRepository;

    public UserAnamnesis getByUserId(Long userId) {
        return anamnesisRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Anamnese não encontrada para o usuário ID: " + userId));
    }

    public UserAnamnesis create(Long userId, UserAnamnesis anamnesisData) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (anamnesisRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("O usuário já possui uma anamnese cadastrada.");
        }

        anamnesisData.setUser(user);
        return anamnesisRepository.save(anamnesisData);
    }

    public UserAnamnesis update(Long userId, UserAnamnesis updatedData) {
        UserAnamnesis existing = anamnesisRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Anamnese não encontrada para o usuário ID: " + userId));

        updatedData.setId(existing.getId());
        updatedData.setUser(existing.getUser());
        return anamnesisRepository.save(updatedData);
    }

    public void delete(Long userId) {
        UserAnamnesis anamnesis = anamnesisRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Anamnese não encontrada para o usuário ID: " + userId));

        anamnesisRepository.delete(anamnesis);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updatePartial(Long userId, String field, String value) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        UserAnamnesis anamnesis = anamnesisRepository.findByUserId(userId)
                .orElse(UserAnamnesis.builder().user(user).build());

        try {
            switch (field) {
                case "mainGoal":
                    anamnesis.setMainGoal(com.xertica.entity.enums.anamnesis.MainGoalType.valueOf(value));
                    break;
                case "activityType":
                    anamnesis.setActivityType(com.xertica.entity.enums.anamnesis.ActivityTypeEnum.valueOf(value));
                    break;
                case "frequency":
                    anamnesis.setFrequency(com.xertica.entity.enums.anamnesis.FrequencyType.valueOf(value));
                    break;
                case "sleepQuality":
                    anamnesis.setSleepQuality(com.xertica.entity.enums.anamnesis.SleepQualityType.valueOf(value));
                    break;
                case "wakesDuringNight":
                    anamnesis.setWakesDuringNight(com.xertica.entity.enums.anamnesis.WakesDuringNightType.valueOf(value));
                    break;
                case "bowelFrequency":
                    anamnesis.setBowelFrequency(com.xertica.entity.enums.anamnesis.BowelFrequencyType.valueOf(value));
                    break;
                case "stressLevel":
                    anamnesis.setStressLevel(com.xertica.entity.enums.anamnesis.StressLevelType.valueOf(value));
                    break;
                case "alcoholUse":
                    anamnesis.setAlcoholUse(com.xertica.entity.enums.anamnesis.AlcoholUseType.valueOf(value));
                    break;
                case "hydrationLevel":
                    anamnesis.setHydrationLevel(com.xertica.entity.enums.anamnesis.HydrationLevelType.valueOf(value));
                    break;
                case "smoking":
                    anamnesis.setSmoking(Boolean.parseBoolean(value));
                    break;
                case "continuousMedication":
                    anamnesis.setContinuousMedication(Boolean.parseBoolean(value));
                    break;
                case "activityMinutesPerDay":
                    anamnesis.setActivityMinutesPerDay(Integer.parseInt(value));
                    break;
                // Strings livres
                case "medicalConditions":
                    anamnesis.setMedicalConditions(value);
                    break;
                case "allergies":
                    anamnesis.setAllergies(value);
                    break;
                case "surgeries":
                    anamnesis.setSurgeries(value);
                    break;
                default:
                    throw new IllegalArgumentException("Campo desconhecido: " + field);
            }
        } catch (IllegalArgumentException e) {
             // Caso a IA envie um valor de Enum inválido
             throw new IllegalArgumentException("Valor inválido '" + value + "' para o campo " + field);
        }

        anamnesisRepository.save(anamnesis);
    }
}
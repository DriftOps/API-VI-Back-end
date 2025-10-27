package com.xertica.mapper;

import com.xertica.dto.UserProfileDTO;
import com.xertica.entity.User;
import com.xertica.entity.UserAnamnesis;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserProfileDTO toUserProfileDTO(User user) {
        if (user == null) {
            return null;
        }

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setWeight(user.getWeight());
        dto.setHeight(user.getHeight());
        dto.setBirthDate(user.getBirthDate() != null ? user.getBirthDate().toString() : null);
        dto.setApproved(user.getApproved());
        dto.setPlan(user.getPlan());

        dto.setDietaryPreferences(
            user.getPreferences().stream()
                .map(p -> p.getPreference().getName())
                .collect(Collectors.toList())
        );
        dto.setRestrictions(
            user.getRestrictions().stream()
                .map(r -> r.getRestriction().getName())
                .collect(Collectors.toList())
        );

        // ✅ MAPEAMENTO COMPLETO DA ANAMNESE
        UserAnamnesis anamnesis = user.getAnamnesis();
        if (anamnesis != null) {
            dto.setGoal(anamnesis.getMainGoal() != null ? anamnesis.getMainGoal().name() : null);
            dto.setActivityLevel(anamnesis.getActivityType());
            dto.setMedicalConditions(anamnesis.getMedicalConditions());
            dto.setAllergies(anamnesis.getAllergies());
            dto.setSurgeries(anamnesis.getSurgeries());
            dto.setFrequency(anamnesis.getFrequency());
            dto.setActivityMinutesPerDay(anamnesis.getActivityMinutesPerDay());
            dto.setSleepQuality(anamnesis.getSleepQuality());
            dto.setWakesDuringNight(anamnesis.getWakesDuringNight());
            dto.setBowelFrequency(anamnesis.getBowelFrequency());
            dto.setAlcoholUse(anamnesis.getAlcoholUse());
            dto.setSmoking(anamnesis.getSmoking());
            dto.setHydrationLevel(anamnesis.getHydrationLevel());
            dto.setContinuousMedication(anamnesis.getContinuousMedication());
        }

        return dto;
    }
}
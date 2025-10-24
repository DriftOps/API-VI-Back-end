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
}
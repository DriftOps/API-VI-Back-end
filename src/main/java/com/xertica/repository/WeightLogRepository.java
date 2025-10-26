package com.xertica.repository;

import com.xertica.entity.User;
import com.xertica.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {
    
    // Busca todo o histórico de um usuário, ordenado por data
    List<WeightLog> findByUserOrderByLogDateAsc(User user);
    
    // Busca o primeiro registro (peso inicial)
    Optional<WeightLog> findTopByUserOrderByLogDateAsc(User user);
    
    // Busca o último registro (peso atual)
    Optional<WeightLog> findTopByUserOrderByLogDateDesc(User user);
}
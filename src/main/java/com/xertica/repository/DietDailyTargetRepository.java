package com.xertica.repository;

import com.xertica.entity.DietDailyTarget;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietDailyTargetRepository extends JpaRepository<DietDailyTarget, Long> {

    Optional<DietDailyTarget> findByDietIdAndTargetDate(Long dietId, LocalDate targetDate);

    List<DietDailyTarget> findByDietIdAndTargetDateBetweenOrderByTargetDateAsc(Long dietId, LocalDate startDate, LocalDate endDate);

}
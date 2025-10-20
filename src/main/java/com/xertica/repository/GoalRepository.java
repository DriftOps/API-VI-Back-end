package com.xertica.repository;

import com.xertica.entity.Goal;
import com.xertica.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser(User user);
}

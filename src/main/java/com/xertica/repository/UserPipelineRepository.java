package com.nutrix.repository;

import com.nutrix.model.User;
import com.nutrix.model.UserPipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPipelineRepository extends JpaRepository<UserPipeline, Long> {
    List<UserPipeline> findByUserOrderByStepOrder(User user);
}

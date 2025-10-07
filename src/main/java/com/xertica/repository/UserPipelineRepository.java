package com.xertica.repository;

import com.xertica.entity.User;
import com.xertica.entity.UserPipeline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPipelineRepository extends JpaRepository<UserPipeline, Long> {
    List<UserPipeline> findByUserOrderByStepOrder(User user);
}

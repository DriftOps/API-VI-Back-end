package com.xertica.repository;

import com.xertica.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import com.xertica.entity.User;
import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUser(User user);
    void deleteByUser(User user);
}
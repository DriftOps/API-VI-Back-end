package com.xertica.repository;

import com.xertica.entity.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import com.xertica.entity.User;
import java.util.List;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {
    List<UserRestriction> findByUser(User user);
    void deleteByUser(User user);
}
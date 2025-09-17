package com.xertica.repository;

import com.xertica.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

public interface UserRepository extends JpaRepository<User, Long> {
    // você pode adicionar métodos custom aqui se precisar no futuro
}

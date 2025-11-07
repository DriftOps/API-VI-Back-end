package com.xertica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.xertica.entity") // aponta p/ entidades
@EnableJpaRepositories(basePackages = "com.xertica.repository") // aponta p/ repositórios
public class ApiViBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiViBackendApplication.class, args);
    }
}
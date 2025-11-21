package com.xertica.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpMethod;
import com.xertica.entity.User;
import com.xertica.repository.UserRepository;
import com.xertica.security.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private final UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/public/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userRepository);
    }

    public static class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final UserRepository userRepository;

        public JwtAuthenticationFilter(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            String path = request.getServletPath();
            String method = request.getMethod();

            System.out.println("=== FILTRO JWT - INÍCIO ===");
            System.out.println("Request: " + method + " " + path);

            // Rotas públicas - não requerem autenticação
            if (path.equals("/api/users/login") || path.equals("/api/users/signup")) {
                System.out.println("Rota pública, passando direto...");
                filterChain.doFilter(request, response);
                return;
            }

            // Para rotas protegidas, verifica o token
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            System.out.println("Authorization header: " + authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    var claims = JwtUtils.validateToken(token).getBody();
                    String email = claims.getSubject();
                    System.out.println("Token válido para email: " + email);

                    User user = userRepository.findByEmail(email).orElse(null);
                    if (user != null) {
                        System.out.println("Usuário encontrado no BD:");
                        System.out.println("  - ID: " + user.getId());
                        System.out.println("  - Nome: " + user.getName());
                        System.out.println("  - Role: " + user.getRole());
                        System.out.println("  - Aprovado: " + user.getApproved());

                        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                        var authorities = List.of(authority);
                        System.out.println("  - Authority: " + authority.getAuthority());

                        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        System.out.println("Autenticação configurada no SecurityContext");
                    } else {
                        System.out.println("❌ Usuário NÃO encontrado no BD para email: " + email);
                    }
                } catch (Exception e) {
                    System.out.println("❌ Erro ao validar token: " + e.getMessage());
                }
            } else {
                System.out.println("❌ Token não encontrado ou formato inválido");
            }

            // Verifica o que ficou no SecurityContext
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                System.out.println("✅ SecurityContext contém autenticação:");
                System.out.println("  - Principal: " + authentication.getPrincipal());
                System.out.println("  - Authorities: " + authentication.getAuthorities());
            } else {
                System.out.println("❌ SecurityContext SEM autenticação");
            }

            System.out.println("=== FILTRO JWT - FIM ===");
            filterChain.doFilter(request, response);
        }
    }
}
package com.manitascrochet.backend.security;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    public SecurityConfig(AuthTokenFilter authTokenFilter) {
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // --------------------------------------------------
                        // Usuarios autenticados
                        // --------------------------------------------------
                        .requestMatchers(HttpMethod.GET, "/api/favorito/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/favorito/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/comentarios/**").authenticated()
                         .requestMatchers(HttpMethod.DELETE, "/api/comentarios/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/valoraciones/**").authenticated()
                        // --------------------------------------------------
                        // Escritura solo para ADMIN
                        // --------------------------------------------------
                        .requestMatchers("/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        // --------------------------------------------------
                        // Consulta polico para todos
                        // --------------------------------------------------
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .anyRequest().authenticated())
                // ─── Handlers para errores de Spring Security ──────────────────
                .exceptionHandling(ex -> ex

                        // Token ausente o inválido → 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("""
                                    {
                                        "status": 401,
                                        "error": "UNAUTHORIZED",
                                        "mensaje": "No autenticado. Por favor inicia sesión.",
                                        "timestamp": "%s",
                                        "fieldErrors": null
                                    }
                                    """.formatted(LocalDateTime.now()));
                        })

                        // Token válido pero sin permisos → 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(403);
                            response.getWriter().write("""
                                    {
                                        "status": 403,
                                        "error": "ACCESS_DENIED",
                                        "mensaje": "No tienes permisos para realizar esta acción.",
                                        "timestamp": "%s",
                                        "fieldErrors": null
                                    }
                                    """.formatted(LocalDateTime.now()));
                        }))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
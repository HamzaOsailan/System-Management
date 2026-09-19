
        package com.sahab.demo.security;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =============================================
                // CSRF
                // =============================================

                .csrf(csrf -> csrf.disable())

                // =============================================
                // CORS
                // =============================================

                .cors(cors -> {})

                // =============================================
                // SESSION
                // =============================================

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =============================================
                // AUTHORIZATION
                // =============================================

                .authorizeHttpRequests(auth -> auth



                        .requestMatchers(
                                "/auth/**"
                        ).permitAll()


                        .requestMatchers(
                                HttpMethod.GET,
                                "/requests/my"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN",
                                "MANAGER"
                        )

                        // =====================================
                        // CREATE REQUEST
                        // =====================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/requests/CreateRequest"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN"
                        )

                        // =====================================
                        // WORKFLOW
                        // USER + ADMIN + MANAGER
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/requests/*/workflow"
                        ).hasAnyRole(
                                "USER",
                                "ADMIN",
                                "MANAGER"
                        )

                        // =====================================
                        // PENDING APPROVALS
                        // ADMIN + MANAGER
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/requests/pending-approvals"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        // =====================================
                        // APPROVE
                        // ADMIN + MANAGER
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/requests/*/approve"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        // =====================================
                        // REJECT
                        // ADMIN + MANAGER
                        // =====================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/requests/*/reject"
                        ).hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        // =====================================
                        // GET ALL REQUESTS
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/requests"
                        ).hasRole("ADMIN")

                        // =====================================
                        // PAGINATION
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                HttpMethod.GET,
                                "/requests/page"
                        ).hasRole("ADMIN")

                        // =====================================
                        // REQUESTS BY USER
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                "/requests/user/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // REQUESTS BY STATUS
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                "/requests/status/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // USERS
                        // ADMIN ONLY
                        // =====================================

                        .requestMatchers(
                                "/users/**"
                        ).hasRole("ADMIN")

                        // =====================================
                        // NOTIFICATIONS
                        // =====================================

                        .requestMatchers(
                                "/notifications/**"
                        ).authenticated()

                        // =====================================
                        // SLA
                        // =====================================

                        .requestMatchers(
                                "/sla/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                "/me"
                        ).authenticated()

                        // =====================================
                        // AI
                        // =====================================

                        .requestMatchers(
                                "/ai/**"
                        ).authenticated()

                        // =====================================
                        // EVERYTHING ELSE
                        // =====================================

                                .requestMatchers(
                                        "/profile/documents/**"
                                ).authenticated()
                   

                        .anyRequest().authenticated()
                )

                // =============================================
                // JWT FILTER
                // =============================================

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}


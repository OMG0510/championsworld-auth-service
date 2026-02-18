package com.shopping.b2c_ecommerce.config;

import com.shopping.b2c_ecommerce.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // PUBLIC AUTH ENDPOINTS
                        // =========================
                        .requestMatchers(
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/reset-password",
                                "/auth/validate-token",

                                // OTP Login
                                "/auth/login/otp/phone/send",
                                "/auth/login/otp/phone/verify",
                                "/auth/login/otp/email/send",
                                "/auth/login/otp/email/verify",

                                // Registration
                                "/auth/register/email/start",
                                "/auth/register/email/verify",
                                "/auth/register/email/complete",
                                "/auth/register/phone/start",
                                "/auth/register/phone/verify",
                                "/auth/register/phone/complete",

                                // OAuth
                                "/auth/oauth/google"
                        ).permitAll()

                        // =========================
                        // CUSTOMER APIs
                        // =========================
                        .requestMatchers(
                                "/auth/me",
                                "/customer/**"
                        ).hasRole("CUSTOMER")

                        // =========================
                        // SUPER ADMIN APIs
                        // =========================
                        .requestMatchers(
                                "/auth/admin/**",
                                "/auth/register/admin",
                                "/auth/show-admins"
                        ).hasRole("SUPER_ADMIN")

                        // =========================
                        // EVERYTHING ELSE
                        // =========================
                        .anyRequest().denyAll()
                )

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, ex1) ->
                                response.sendError(403, "ACCESS DENIED")
                        )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
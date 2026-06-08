package in.bawvpl.Authify.config;

import in.bawvpl.Authify.filter.JwtRequestFilter;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    // =====================================================
    // AUTH MANAGER
    // =====================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // =====================================================
                // CORS
                // =====================================================

                .cors(cors ->

                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                // =====================================================
                // CSRF
                // =====================================================

                .csrf(csrf ->
                        csrf.disable()
                )

                // =====================================================
                // SESSION
                // =====================================================

                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // =====================================================
                // EXCEPTION HANDLING
                // =====================================================

                .exceptionHandling(ex -> ex

                        .authenticationEntryPoint((req, res, e) -> {

                            res.setStatus(
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );

                            res.setContentType(
                                    "application/json"
                            );

                            res.setCharacterEncoding("UTF-8");

                            res.getWriter().write(
                                    """
                                    {
                                      "success": false,
                                      "status": 401,
                                      "message": "Unauthorized"
                                    }
                                    """
                            );
                        })

                        .accessDeniedHandler((req, res, e) -> {

                            res.setStatus(
                                    HttpServletResponse.SC_FORBIDDEN
                            );

                            res.setContentType(
                                    "application/json"
                            );

                            res.setCharacterEncoding("UTF-8");

                            res.getWriter().write(
                                    """
                                    {
                                      "success": false,
                                      "status": 403,
                                      "message": "Forbidden"
                                    }
                                    """
                            );
                        })
                )

                // =====================================================
                // AUTHORIZATION
                // =====================================================

                .authorizeHttpRequests(auth -> auth

                        // =====================================================
                        // OPTIONS
                        // =====================================================

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =====================================================
                        // PUBLIC ROUTES
                        // =====================================================

                        .requestMatchers(

                                "/",

                                "/error",

                                "/favicon.ico",

                                // =====================================================
                                // AUTH
                                // =====================================================

                                "/api/v1.0/register",

                                "/api/v1.0/login",

                                "/api/v1.0/login/**",

                                "/api/v1.0/verify/**",

                                "/api/v1.0/verify-email/**",

                                "/api/v1.0/verify-otp/**",

                                "/api/v1.0/refresh-token",

                                "/api/v1.0/forgot-password/**",

                                "/api/v1.0/reset-password/**",

                                "/api/v1.0/resend-verification-email/**",

                                // =====================================================
                                // ADMIN AUTH
                                // =====================================================

                                "/api/v1.0/admin/auth/**",

                                // =====================================================
                                // 2FA
                                // =====================================================

                                "/api/v1.0/2fa/**",

                                // =====================================================
                                // PAYMENT VERIFY
                                // =====================================================

                                "/api/v1.0/payment/verify/**",

                                // =====================================================
                                // PUBLIC APIS
                                // =====================================================

                                "/api/v1.0/public/**",

                                "/api/v1.0/application/public/**",

                                "/api/v1.0/application/public",

                                "/api/v1.0/application/list",

                                "/api/v1.0/application/health",

                                // =====================================================
                                // STATIC FILES
                                // =====================================================

                                "/uploads/**",

                                "/files/**",

                                "/images/**",

                                // =====================================================
                                // ACTUATOR
                                // =====================================================

                                "/actuator/**",

                                // =====================================================
                                // SWAGGER
                                // =====================================================

                                "/swagger-ui/**",

                                "/swagger-ui.html",

                                "/v3/api-docs/**"

                        ).permitAll()

                        // =====================================================
                        // ADMIN ROUTES
                        // =====================================================

                        .requestMatchers(

                                "/api/v1.0/admin/**"

                        ).hasRole("ADMIN")

                        // =====================================================
                        // AUTHENTICATED USER ROUTES
                        // =====================================================

                        .requestMatchers(

                                "/api/v1.0/profile/**",

                                "/api/v1.0/settings/**",

                                "/api/v1.0/activity/**",

                                "/api/v1.0/favorites/**",

                                "/api/v1.0/notifications/**",

                                "/api/v1.0/notifications/admin/**",

                                "/api/v1.0/tickets/**",

                                "/api/v1.0/application/**",

                                "/api/v1.0/kyc/**",

                                "/api/v1.0/dashboard/**",

                                "/api/v1.0/orders/**",

                                "/api/v1.0/payment/**",

                                "/api/v1.0/subscription/**",

                                "/api/v1.0/user/**",

                                "/api/v1.0/logout"

                        ).authenticated()

                        // =====================================================
                        // EVERYTHING ELSE
                        // =====================================================

                        .anyRequest()
                        .authenticated()
                )

                // =====================================================
                // JWT FILTER
                // =====================================================

                .addFilterBefore(

                        jwtRequestFilter,

                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // =====================================================
    // CORS CONFIGURATION
    // =====================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        // =====================================================
        // ALLOWED ORIGINS
        // =====================================================

        config.setAllowedOriginPatterns(

                List.of(

                        "http://localhost:3000",

                        "http://localhost:5173",

                        "http://localhost:5174",

                        "http://127.0.0.1:5173",

                        "http://127.0.0.1:5174",

                        "http://43.205.116.38",

                        "https://43.205.116.38",

                        "https://boldandwise.duckdns.org/"
                )
        );

        // =====================================================
        // METHODS
        // =====================================================

        config.setAllowedMethods(

                List.of(

                        "GET",

                        "POST",

                        "PUT",

                        "PATCH",

                        "DELETE",

                        "OPTIONS"
                )
        );

        // =====================================================
        // HEADERS
        // =====================================================

        config.setAllowedHeaders(
                List.of("*")
        );

        // =====================================================
        // EXPOSED HEADERS
        // =====================================================

        config.setExposedHeaders(

                List.of(

                        "Authorization",

                        "Content-Type"
                )
        );

        // =====================================================
        // CREDENTIALS
        // =====================================================

        config.setAllowCredentials(true);

        // =====================================================
        // CACHE
        // =====================================================

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }
}
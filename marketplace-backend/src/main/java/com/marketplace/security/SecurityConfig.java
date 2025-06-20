package com.marketplace.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Product browsing
                        .requestMatchers("/api/products", "/api/products/**").permitAll()
                        .requestMatchers("/api/categories", "/api/categories/**").permitAll()
                        .requestMatchers("/api/reviews/product/**", "/api/reviews/stats/**").permitAll()

                        // Search & recommendations
                        .requestMatchers("/api/search/**").permitAll()
                        .requestMatchers("/api/recommendations/similar/**").permitAll()
                        .requestMatchers("/api/recommendations/trending").permitAll()
                        .requestMatchers("/api/recommendations/cross-sell/**").permitAll()
                        .requestMatchers("/api/recommendations/new-user").permitAll()

                        // File access
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/static/**").permitAll()

                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()

                        // API Documentation (public)
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()

                        // WebSocket endpoint (will be authenticated via JWT in headers)
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws-native/**").permitAll()

                        // Admin dashboard & management
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .requestMatchers("/api/recommendations/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/loyalty/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/categories/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/reviews/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/broadcast").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/send").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/admin/**").hasRole("ADMIN")

                        // Advanced monitoring
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // User profile & account management
                        .requestMatchers("/api/profile/**").authenticated()

                        // Shopping cart & orders
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()

                        // Payments
                        .requestMatchers("/api/payments/**").authenticated()

                        // Reviews
                        .requestMatchers("/api/reviews").authenticated()
                        .requestMatchers("/api/reviews/my-reviews").authenticated()
                        .requestMatchers("/api/reviews/check/**").authenticated()

                        // Recommendations
                        .requestMatchers("/api/recommendations/for-you").authenticated()
                        .requestMatchers("/api/recommendations/track").authenticated()
                        .requestMatchers("/api/recommendations/category/**").authenticated()
                        .requestMatchers("/api/recommendations/recently-viewed").authenticated()
                        .requestMatchers("/api/recommendations/preferences").authenticated()
                        .requestMatchers("/api/recommendations/price-range").authenticated()

                        // Loyalty system
                        .requestMatchers("/api/loyalty/**").authenticated()

                        // Chat system
                        .requestMatchers("/api/chat/**").authenticated()

                        // Notifications
                        .requestMatchers("/api/notifications").authenticated()
                        .requestMatchers("/api/notifications/unread-count").authenticated()
                        .requestMatchers("/api/notifications/mark-all-read").authenticated()

                        // File uploads
                        .requestMatchers("/api/upload/**").authenticated()

                        // Dashboard access
                        .requestMatchers("/api/dashboard/user/**").authenticated()
                        .requestMatchers("/api/dashboard/seller/**").authenticated()
                        .requestMatchers("/api/dashboard/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:4200",
                "https://*.vercel.app",
                "https://*.netlify.app",
                "https://yourdomain.com"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Requested-With",
                "X-Auth-Token"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Authorization"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
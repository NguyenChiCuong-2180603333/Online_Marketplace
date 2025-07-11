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

import com.marketplace.service.UserService;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserService userService;

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
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/chat/test-broadcast").permitAll()
                .requestMatchers("/ws/**", "/ws-native/**", "/topic/**", "/user/**", "/queue/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                .requestMatchers("/api/categories", "/api/categories/**").permitAll()
                .requestMatchers("/api/reviews/product/**", "/api/reviews/stats/**").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/search/**").permitAll()
                .requestMatchers("/api/recommendations/similar/**").permitAll()
                .requestMatchers("/api/recommendations/trending").permitAll()
                .requestMatchers("/api/recommendations/cross-sell/**").permitAll()
                .requestMatchers("/api/recommendations/new-user").permitAll()
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/recommendations/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/loyalty/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/categories/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/reviews/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/notifications/broadcast").hasRole("ADMIN")
                .requestMatchers("/api/notifications/send").hasRole("ADMIN")
                .requestMatchers("/api/notifications/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/profile/**").authenticated()
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers("/api/orders/**").authenticated()
                .requestMatchers("/api/payments/**").authenticated()
                .requestMatchers("/api/reviews").authenticated()
                .requestMatchers("/api/reviews/my-reviews").authenticated()
                .requestMatchers("/api/reviews/check/**").authenticated()
                .requestMatchers("/api/recommendations/for-you").authenticated()
                .requestMatchers("/api/recommendations/track").authenticated()
                .requestMatchers("/api/recommendations/category/**").authenticated()
                .requestMatchers("/api/recommendations/recently-viewed").authenticated()
                .requestMatchers("/api/recommendations/preferences").authenticated()
                .requestMatchers("/api/recommendations/price-range").authenticated()
                .requestMatchers("/api/loyalty/**").authenticated()
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/notifications").authenticated()
                .requestMatchers("/api/notifications/unread-count").authenticated()
                .requestMatchers("/api/notifications/mark-all-read").authenticated()
                .requestMatchers("/api/upload/**").authenticated()
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
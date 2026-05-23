package com.bitdance.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${bitdance.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        List<String> origins = new ArrayList<>(Arrays.asList(allowedOrigins));
        // Local Vite may fall back to 5174 when 5173 is already occupied.
        origins.add("http://localhost:5174");
        origins.add("http://127.0.0.1:5174");
        cfg.setAllowedOriginPatterns(origins.stream().distinct().toList());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("X-Trace-Id"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}

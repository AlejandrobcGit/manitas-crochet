package com.manitascrochet.backend.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

// Requiere la dependencia com.github.ben-manes.caffeine:caffeine en el pom/build.gradle
@Configuration
@EnableCaching
public class DashboardCacheConfig {

        @Bean
        public CacheManager cacheManager() {
                CaffeineCacheManager cacheManager = new CaffeineCacheManager("dashboardKpis");

                // TTL corto: el dashboard no necesita ser exacto al segundo, pero sí
                // evitar recalcular todas las agregaciones en cada refresh de pantalla.
                // Ajustá expireAfterWrite según qué tan "fresco" necesitás el dato.
                cacheManager.setCaffeine(
                                Caffeine.newBuilder()
                                                .expireAfterWrite(30, TimeUnit.SECONDS)
                                                .maximumSize(10));

                return cacheManager;
        }
}

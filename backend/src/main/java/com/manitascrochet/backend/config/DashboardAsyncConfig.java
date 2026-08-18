package com.manitascrochet.backend.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class DashboardAsyncConfig {

        // Pool dedicado y acotado: evita competir con el ForkJoinPool.commonPool()
        // (que Spring/otros usan para otras cosas) y evita crear threads sin límite.
        @Bean(name = "dashboardExecutor")
        public Executor dashboardExecutor() {
                ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
                executor.setCorePoolSize(4);
                executor.setMaxPoolSize(8);
                executor.setQueueCapacity(50);
                executor.setThreadNamePrefix("dashboard-");
                executor.initialize();
                return executor;
        }
}

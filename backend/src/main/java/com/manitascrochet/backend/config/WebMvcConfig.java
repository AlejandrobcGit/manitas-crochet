package com.manitascrochet.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Use the legacy Ant path matcher for Spring MVC resource handling
        configurer.setPatternParser(null);
        configurer.setPathMatcher(new AntPathMatcher());
    }
}
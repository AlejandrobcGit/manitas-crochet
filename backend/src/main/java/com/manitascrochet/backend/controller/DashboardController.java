package com.manitascrochet.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.dto.DashboardResponseDto;
import com.manitascrochet.backend.service.DashboardSevice;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardSevice dashboardSevice;

    @GetMapping("/kpis")
    public DashboardResponseDto getKpis() {
        return dashboardSevice.getKpis();
    }
}
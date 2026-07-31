package com.manitascrochet.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.service.ValoracionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/valoraciones")
@RequiredArgsConstructor
@Validated
public class ValoracionController {

    private final ValoracionService valoracionService;

    @PostMapping("/{figuraId}")
    public ResponseEntity<Void> valorarFigura(
            @PathVariable String figuraId,
            @RequestBody ValoracionDto  valoracionDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        valoracionService.valorarFigura(
                figuraId,
                valoracionDto.getPuntuacion(),
                userDetails);

        return ResponseEntity.ok().build();
    }
}
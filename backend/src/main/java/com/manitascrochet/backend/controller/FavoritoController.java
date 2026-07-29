package com.manitascrochet.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.service.FavoritoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/favorito")
@RequiredArgsConstructor
public class FavoritoController {

    private final FavoritoService favoritoService;

    @PostMapping("/{figuraId}")
    public ResponseEntity<Boolean> cambiarFavorito(
            @PathVariable String figuraId,
            Authentication authentication) {

        String username = authentication.getName();

        boolean resultado = favoritoService.cambiarFavorito(
                username,
                figuraId);

        return ResponseEntity.ok(resultado);
    }

    @GetMapping
    public ResponseEntity<List<String>> obtenerFavoritos(
            Authentication authentication) {

        return ResponseEntity.ok(
                favoritoService.obtenerFavoritosActivos(
                        authentication.getName()));
    }

}

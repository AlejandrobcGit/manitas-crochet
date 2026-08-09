package com.manitascrochet.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.dto.ComentarioDto;
import com.manitascrochet.backend.dto.ComentarioResponseDto;
import com.manitascrochet.backend.model.Comentario;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.service.ComentarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @DeleteMapping("/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(
            @PathVariable String comentarioId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        comentarioService.eliminarComentario(comentarioId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/figura/{figuraId}")
    public ResponseEntity<List<ComentarioResponseDto>> obtenerComentariosFigura(
            @PathVariable String figuraId) {

        List<ComentarioResponseDto> comentarios = comentarioService.obtenerComentariosFigura(figuraId);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping
    public ResponseEntity<Comentario> guardarComentario(
            @Valid @RequestBody ComentarioDto comentarioDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Comentario guardado = comentarioService.guardarComentario(comentarioDto, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/figura/{figuraId}/usuario")
    public ResponseEntity<Comentario> obtenerComentarioUsuarioFigura(
            @PathVariable String figuraId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Comentario comentario = comentarioService.obtenerComentarioUsuarioFigura(
                figuraId, userDetails.getId());

        if (comentario == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(comentario);
    }
}
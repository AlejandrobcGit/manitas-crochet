package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.dto.ResumenValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ValoracionInvalidaException;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.repository.ValoracionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;

    public void valorarFigura(
            String figuraId,
            Integer puntuacion,
            UserDetailsImpl userDetails) {

        // Usuario obligatorio
        if (userDetails == null) {
            return;
        }

        // RN-02: la puntuación debe estar entre 1 y 5
        if (puntuacion == null || puntuacion < 1 || puntuacion > 5) {
            throw new ValoracionInvalidaException();
        }

        Valoracion valoracion = valoracionRepository
                .findByUsuarioIdAndFiguraId(
                        userDetails.getId(),
                        figuraId)
                .orElse(null);

        // RN-01: un usuario solo puede tener una valoración por figura
        if (valoracion != null) {

            valoracion.setPuntuacion(puntuacion);
            valoracion.setFecha(LocalDateTime.now());

        } else {

            valoracion = new Valoracion();
            valoracion.setUsuarioId(userDetails.getId());
            valoracion.setFiguraId(figuraId);
            valoracion.setPuntuacion(puntuacion);
            valoracion.setFecha(LocalDateTime.now());
        }

        valoracionRepository.save(valoracion);
    }

    public ResumenValoracionDto obtenerResumenValoraciones(
            String figuraId) {

        List<Valoracion> valoraciones = valoracionRepository.findByFiguraId(figuraId);

        double media = valoraciones.stream()
                .mapToInt(Valoracion::getPuntuacion)
                .average()
                .orElse(0.0);

        return new ResumenValoracionDto(
                media,
                (long) valoraciones.size());
    }
}
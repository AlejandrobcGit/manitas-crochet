package com.manitascrochet.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Visualizacion;
import com.manitascrochet.backend.repository.VisualizacionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisualizacionService {

    private final VisualizacionRepository visualizacionRepository;

public void marcarVisualizacion(String figuraId, UserDetailsImpl userDetails) {

    if (userDetails != null &&
        userDetails.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + Rol.ADMIN.name()))) {
        return;
    }

    Visualizacion visualizacion = new Visualizacion();

    if (userDetails != null) {
        visualizacion.setUsuarioId(userDetails.getId());
    }

    visualizacion.setFiguraId(figuraId);
    visualizacion.setFecha(LocalDateTime.now());

    visualizacionRepository.save(visualizacion);
}
}

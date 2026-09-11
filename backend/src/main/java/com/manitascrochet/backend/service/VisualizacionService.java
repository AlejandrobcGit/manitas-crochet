package com.manitascrochet.backend.service;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Visualizacion;
import com.manitascrochet.backend.repository.VisualizacionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VisualizacionService {

    private final VisualizacionRepository visualizacionRepository;
    private final MongoTemplate mongoTemplate;

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

        // MEJORA 3: incremento atómico del contador desnormalizado de visualizaciones.
        // Con $inc no hace falta recargar y re-guardar toda la figura; MongoDB gestiona
        // el incremento en servidor y crea el campo a 0→1 si aún no existía.
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("id").is(figuraId)),
                new Update().inc("numVisualizaciones", 1),
                Figura.class);
    }
}

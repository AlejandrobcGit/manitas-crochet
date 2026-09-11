package com.manitascrochet.backend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.repository.ValoracionRepository;
import com.manitascrochet.backend.repository.VisualizacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MEJORA 3: migración única hacia métricas denormalizadas.
 *
 * Puebla puntuacionMedia / totalValoraciones / numVisualizaciones en las figuras
 * que todavía no las tienen (datos previos a la denormalización, o figuras recién
 * creadas sin ninguna valoración). A partir de este punto, los servicios de
 * Valoracion y Visualizacion mantienen estos campos al día al escribir.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackfillFigurasMetricasRunner implements CommandLineRunner {

    private final FiguraRepository figuraRepository;
    private final ValoracionRepository valoracionRepository;
    private final VisualizacionRepository visualizacionRepository;

    @Override
    public void run(String... args) {

        List<Figura> pendientes = figuraRepository.findByPuntuacionMediaIsNull();

        if (pendientes.isEmpty()) {
            return;
        }

        for (Figura figura : pendientes) {

            // Media y total de valoraciones de la figura
            List<Valoracion> valoraciones = valoracionRepository.findByFiguraId(figura.getId());

            figura.setPuntuacionMedia(valoraciones.stream()
                    .mapToInt(Valoracion::getPuntuacion)
                    .average()
                    .orElse(0.0));
            figura.setTotalValoraciones((long) valoraciones.size());
            figura.setNumVisualizaciones(visualizacionRepository.countByFiguraId(figura.getId()));
        }

        figuraRepository.saveAll(pendientes);
        log.info("Métricas denormalizadas calculadas para {} figuras", pendientes.size());
    }
}
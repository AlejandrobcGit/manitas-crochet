package com.manitascrochet.backend.test;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Dificultad;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.FiguraRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Runner TEMPORAL de un solo uso para probar la paginación.
// Inserta 100 figuras de ejemplo SIN imágenes cuando la propiedad
// app.seed-figuras.enabled=true. Desactívalo o elimina este archivo al terminar.
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SeedFigurasTemporalRunner {

    private final FiguraRepository figuraRepository;
    private final CategoriaRepository categoriaRepository;

    @Bean
    @ConditionalOnProperty(name = "app.seed-figuras.enabled", havingValue = "true")
    CommandLineRunner seedFiguras() {
        return args -> {

            log.info("=== Insertando 100 figuras de ejemplo (sin imágenes) ===");

            String nombreCategoria = "Figuras de ejemplo";
            Categoria categoria = categoriaRepository
                    .findByNombreIgnoreCase(nombreCategoria)
                    .orElseGet(() -> {
                        Categoria nueva = new Categoria();
                        nueva.setNombre(nombreCategoria);
                        return categoriaRepository.save(nueva);
                    });

            Dificultad[] dificultades = Dificultad.values();

            for (int i = 1; i <= 100; i++) {

                Figura figura = new Figura();
                figura.setNombre("Figura de ejemplo Nº " + i);
                figura.setDescripcion("Figura amigurumi de ejemplo " + i + " para probar la paginación.");
                figura.setCategoriaId(categoria.getId());
                figura.setDificultad(dificultades[i % dificultades.length]);
                figura.setAutor("Seed");
                figura.setImagenPrincipal(null);
                figura.setFileId_imagenPrincipal(null);
                figura.setImagenesSecundarias(List.of());
                figura.setFileId_imagenesSecundarias(List.of());
                figura.setColoresIds(List.of());
                figura.setAltura(5 + (i % 15));
                figura.setAncho(4 + (i % 12));
                figura.setPeso(50 + (i % 200));
                figura.setFechaCreacion(LocalDateTime.now().minusMinutes(i));
                figura.setFechaModificacion(LocalDateTime.now().minusMinutes(i));

                figuraRepository.save(figura);
            }

            log.info("=== 100 figuras de ejemplo insertadas ===");
        };
    }
}
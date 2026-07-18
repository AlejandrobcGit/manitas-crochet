package com.manitascrochet.backend.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.model.Dificultad;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final CategoriaRepository categoriaRepository;
    private final ColorRepository colorRepository;
    private final FiguraRepository figuraRepository;

    @Bean
    CommandLineRunner cargarDatos() {

        return args -> {

            cargarCategorias();
            cargarColores();
            cargarFiguras();

            System.out.println("✅ Datos de ejemplo cargados");
        };
    }

    private void cargarCategorias() {

        if (categoriaRepository.count() > 0) {
            return;
        }

        categoriaRepository.save(crearCategoria("Anime"));
        categoriaRepository.save(crearCategoria("Animales"));
        categoriaRepository.save(crearCategoria("Fantasía"));
        categoriaRepository.save(crearCategoria("Personajes"));
        categoriaRepository.save(crearCategoria("Navidad"));
        System.out.println("✅ Categorías de ejemplo cargadas");
    }

    private void cargarColores() {

        if (colorRepository.count() > 0) {
            return;
        }

        colorRepository.save(crearColor("Rojo", "#FF0000"));
        colorRepository.save(crearColor("Azul", "#0000FF"));
        colorRepository.save(crearColor("Verde", "#00FF00"));
        colorRepository.save(crearColor("Amarillo", "#FFFF00"));
        colorRepository.save(crearColor("Negro", "#000000"));
        colorRepository.save(crearColor("Blanco", "#FFFFFF"));
        colorRepository.save(crearColor("Rosa", "#FFC0CB"));
        colorRepository.save(crearColor("Marrón", "#8B4513"));

    }

    private void cargarFiguras() {

        if (figuraRepository.count() > 0) {
            return;
        }

        Categoria anime = categoriaRepository.findByNombreIgnoreCase("Anime").orElseThrow();
        Categoria animales = categoriaRepository.findByNombreIgnoreCase("Animales").orElseThrow();
        Categoria fantasia = categoriaRepository.findByNombreIgnoreCase("Fantasía").orElseThrow();
        Categoria personajes = categoriaRepository.findByNombreIgnoreCase("Personajes").orElseThrow();
        Categoria navidad = categoriaRepository.findByNombreIgnoreCase("Navidad").orElseThrow();

        Color rojo = colorRepository.findByNombreIgnoreCase("Rojo").orElseThrow();
        Color azul = colorRepository.findByNombreIgnoreCase("Azul").orElseThrow();
        Color verde = colorRepository.findByNombreIgnoreCase("Verde").orElseThrow();
        Color amarillo = colorRepository.findByNombreIgnoreCase("Amarillo").orElseThrow();
        Color negro = colorRepository.findByNombreIgnoreCase("Negro").orElseThrow();
        Color blanco = colorRepository.findByNombreIgnoreCase("Blanco").orElseThrow();
        Color rosa = colorRepository.findByNombreIgnoreCase("Rosa").orElseThrow();
        Color marron = colorRepository.findByNombreIgnoreCase("Marrón").orElseThrow();

        figuraRepository.save(crearFigura(
                "Pikachu",
                anime.getId(),
                Dificultad.PRINCIPIANTE,
                List.of(amarillo.getId(), negro.getId())));

        figuraRepository.save(crearFigura(
                "Charmander",
                anime.getId(),
                Dificultad.INTERMEDIO,
                List.of(rojo.getId(), amarillo.getId(), negro.getId())));

        figuraRepository.save(crearFigura(
                "Totoro",
                anime.getId(),
                Dificultad.INTERMEDIO,
                List.of(blanco.getId(), negro.getId())));

        figuraRepository.save(crearFigura(
                "Conejo",
                animales.getId(),
                Dificultad.PRINCIPIANTE,
                List.of(blanco.getId(), rosa.getId())));

        figuraRepository.save(crearFigura(
                "Oso",
                animales.getId(),
                Dificultad.INTERMEDIO,
                List.of(marron.getId())));

        figuraRepository.save(crearFigura(
                "Gato Negro",
                animales.getId(),
                Dificultad.INTERMEDIO,
                List.of(negro.getId())));

        figuraRepository.save(crearFigura(
                "Unicornio",
                fantasia.getId(),
                Dificultad.AVANZADO,
                List.of(blanco.getId(), rosa.getId())));

        figuraRepository.save(crearFigura(
                "Dragón Rojo",
                fantasia.getId(),
                Dificultad.AVANZADO,
                List.of(rojo.getId())));

        figuraRepository.save(crearFigura(
                "Hada Verde",
                fantasia.getId(),
                Dificultad.INTERMEDIO,
                List.of(verde.getId())));

        figuraRepository.save(crearFigura(
                "Mario",
                personajes.getId(),
                Dificultad.INTERMEDIO,
                List.of(rojo.getId(), azul.getId())));

        figuraRepository.save(crearFigura(
                "Luigi",
                personajes.getId(),
                Dificultad.INTERMEDIO,
                List.of(verde.getId(), azul.getId())));

        figuraRepository.save(crearFigura(
                "Sonic",
                personajes.getId(),
                Dificultad.AVANZADO,
                List.of(azul.getId())));

        figuraRepository.save(crearFigura(
                "Papá Noel",
                navidad.getId(),
                Dificultad.INTERMEDIO,
                List.of(rojo.getId(), blanco.getId())));

        figuraRepository.save(crearFigura(
                "Muñeco de Nieve",
                navidad.getId(),
                Dificultad.PRINCIPIANTE,
                List.of(blanco.getId())));

        figuraRepository.save(crearFigura(
                "Rudolf",
                navidad.getId(),
                Dificultad.INTERMEDIO,
                List.of(marron.getId(), rojo.getId())));
        System.out.println("✅ Figuras de ejemplo cargadas");
    }

    private Categoria crearCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoria;
    }

    private Color crearColor(String nombre, String codigo) {
        Color color = new Color();
        color.setNombre(nombre);
        color.setCodigo(codigo);
        return color;
    }

    private Figura crearFigura(
            String nombre,
            String categoriaId,
            Dificultad dificultad,
            List<String> coloresIds) {

        Figura figura = new Figura();

        figura.setNombre(nombre);
        figura.setDescripcion("Figura amigurumi de " + nombre);
        figura.setCategoriaId(categoriaId);
        figura.setDificultad(dificultad);
        figura.setAutor("Admin");

        figura.setImagenPrincipal(
                nombre.toLowerCase().replace(" ", "-") + ".jpg");

        figura.setImagenesSecundarias(List.of(
                nombre.toLowerCase().replace(" ", "-") + "-1.jpg",
                nombre.toLowerCase().replace(" ", "-") + "-2.jpg"));

        figura.setColoresIds(coloresIds);
        figura.setFechaCreacion(LocalDateTime.now());
        figura.setFechaModificacion(LocalDateTime.now());

        return figura;
    }
}
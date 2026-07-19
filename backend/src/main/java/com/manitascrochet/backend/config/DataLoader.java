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

                colorRepository.save(crearColor("Blanco", "#FFFFFF"));
                colorRepository.save(crearColor("Negro", "#000000"));
                colorRepository.save(crearColor("Rojo", "#FF0000"));
                colorRepository.save(crearColor("Verde", "#008000"));
                colorRepository.save(crearColor("Azul", "#0000FF"));
                colorRepository.save(crearColor("Amarillo", "#FFFF00"));
                colorRepository.save(crearColor("Naranja", "#FFA500"));
                colorRepository.save(crearColor("Rosa", "#FFC0CB"));
                colorRepository.save(crearColor("Morado", "#800080"));
                colorRepository.save(crearColor("Marrón", "#8B4513"));
                colorRepository.save(crearColor("Gris", "#808080"));
                colorRepository.save(crearColor("Turquesa", "#40E0D0"));
                colorRepository.save(crearColor("Beige", "#F5F5DC"));
                colorRepository.save(crearColor("Crema", "#FFFDD0"));
                colorRepository.save(crearColor("Coral", "#FF7F50"));
                colorRepository.save(crearColor("Fucsia", "#FF00FF"));
                colorRepository.save(crearColor("Burdeos", "#800020"));
                colorRepository.save(crearColor("Mostaza", "#FFDB58"));
                colorRepository.save(crearColor("Oro", "#FFD700"));
                colorRepository.save(crearColor("Plata", "#C0C0C0"));
                colorRepository.save(crearColor("Verde Oliva", "#808000"));
                colorRepository.save(crearColor("Verde Lima", "#32CD32"));
                colorRepository.save(crearColor("Verde Agua", "#00FA9A"));
                colorRepository.save(crearColor("Verde Esmeralda", "#50C878"));
                colorRepository.save(crearColor("Verde Bosque", "#228B22"));
                colorRepository.save(crearColor("Azul Marino", "#000080"));
                colorRepository.save(crearColor("Azul Cielo", "#87CEEB"));
                colorRepository.save(crearColor("Azul Eléctrico", "#007FFF"));
                colorRepository.save(crearColor("Azul Petróleo", "#005F6A"));
                colorRepository.save(crearColor("Azul Acero", "#4682B4"));
                colorRepository.save(crearColor("Azul Real", "#4169E1"));
                colorRepository.save(crearColor("Azul Bebé", "#89CFF0"));
                colorRepository.save(crearColor("Rojo Oscuro", "#8B0000"));
                colorRepository.save(crearColor("Rojo Vino", "#722F37"));
                colorRepository.save(crearColor("Rojo Tomate", "#FF6347"));
                colorRepository.save(crearColor("Rojo Cereza", "#DE3163"));
                colorRepository.save(crearColor("Rosa Claro", "#FFB6C1"));
                colorRepository.save(crearColor("Rosa Palo", "#E8ADAA"));
                colorRepository.save(crearColor("Rosa Chicle", "#FC74FD"));
                colorRepository.save(crearColor("Salmón", "#FA8072"));
                colorRepository.save(crearColor("Melocotón", "#FFDAB9"));
                colorRepository.save(crearColor("Lavanda", "#E6E6FA"));
                colorRepository.save(crearColor("Violeta", "#8F00FF"));
                colorRepository.save(crearColor("Malva", "#E0B0FF"));
                colorRepository.save(crearColor("Púrpura", "#6A0DAD"));
                colorRepository.save(crearColor("Café", "#6F4E37"));
                colorRepository.save(crearColor("Chocolate", "#7B3F00"));
                colorRepository.save(crearColor("Canela", "#D2691E"));
                colorRepository.save(crearColor("Caramelo", "#AF6E4D"));
                colorRepository.save(crearColor("Arena", "#C2B280"));
                colorRepository.save(crearColor("Camel", "#C19A6B"));
                colorRepository.save(crearColor("Marfil", "#FFFFF0"));
                colorRepository.save(crearColor("Perla", "#EAE0C8"));
                colorRepository.save(crearColor("Hueso", "#E3DAC9"));
                colorRepository.save(crearColor("Gris Claro", "#D3D3D3"));
                colorRepository.save(crearColor("Gris Oscuro", "#A9A9A9"));
                colorRepository.save(crearColor("Gris Grafito", "#383838"));
                colorRepository.save(crearColor("Menta", "#98FF98"));
                colorRepository.save(crearColor("Pistacho", "#93C572"));
                colorRepository.save(crearColor("Manzana", "#8DB600"));
                colorRepository.save(crearColor("Kiwi", "#8EE53F"));
                colorRepository.save(crearColor("Jade", "#00A86B"));
                colorRepository.save(crearColor("Ámbar", "#FFBF00"));
                colorRepository.save(crearColor("Cobre", "#B87333"));
                colorRepository.save(crearColor("Bronce", "#CD7F32"));
                colorRepository.save(crearColor("Ocre", "#CC7722"));
                colorRepository.save(crearColor("Terracota", "#E2725B"));
                colorRepository.save(crearColor("Caqui", "#C3B091"));
                colorRepository.save(crearColor("Amarillo Pastel", "#FDFD96"));
                colorRepository.save(crearColor("Amarillo Canario", "#FFEF00"));
                colorRepository.save(crearColor("Naranja Oscuro", "#FF8C00"));
                colorRepository.save(crearColor("Mandarina", "#F28500"));
                colorRepository.save(crearColor("Calabaza", "#FF7518"));
                colorRepository.save(crearColor("Aguamarina", "#7FFFD4"));
                colorRepository.save(crearColor("Cian", "#00FFFF"));
                colorRepository.save(crearColor("Celeste", "#87CEEB"));
                colorRepository.save(crearColor("Índigo", "#4B0082"));
                colorRepository.save(crearColor("Orquídea", "#DA70D6"));
                colorRepository.save(crearColor("Ciruela", "#8E4585"));
                colorRepository.save(crearColor("Magenta", "#FF00FF"));
                colorRepository.save(crearColor("Granate", "#800000"));
                colorRepository.save(crearColor("Tabaco", "#715D47"));
                colorRepository.save(crearColor("Nogal", "#773F1A"));
                colorRepository.save(crearColor("Color Piel Claro", "#FAD7B5"));
                colorRepository.save(crearColor("Color Piel Medio", "#DDB79A"));
                colorRepository.save(crearColor("Color Piel Oscuro", "#8D5524"));
                colorRepository.save(crearColor("Rosa Bebé", "#F4C2C2"));
                colorRepository.save(crearColor("Azul Bebé", "#89CFF0"));
                colorRepository.save(crearColor("Verde Menta", "#AAF0D1"));
                colorRepository.save(crearColor("Lila Pastel", "#C3B1E1"));
                colorRepository.save(crearColor("Crema Vainilla", "#F3E5AB"));
                System.out.println("✅ Colores de ejemplo cargados");

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
                                List.of(amarillo.getId(), negro.getId()), 10, 5 ));

                figuraRepository.save(crearFigura(
                                "Charmander",
                                anime.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(rojo.getId(), amarillo.getId(), negro.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Totoro",
                                anime.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(blanco.getId(), negro.getId()), 12, 6));

                figuraRepository.save(crearFigura(
                                "Conejo",
                                animales.getId(),
                                Dificultad.PRINCIPIANTE,
                                List.of(blanco.getId(), rosa.getId()), 10, 5));

                figuraRepository.save(crearFigura(
                                "Oso",
                                animales.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(marron.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Gato Negro",
                                animales.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(negro.getId()), 10, 5));

                figuraRepository.save(crearFigura(
                                "Unicornio",
                                fantasia.getId(),
                                Dificultad.AVANZADO,
                                List.of(blanco.getId(), rosa.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Dragón Rojo",
                                fantasia.getId(),
                                Dificultad.AVANZADO,
                                List.of(rojo.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Hada Verde",
                                fantasia.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(verde.getId()), 12, 6));

                figuraRepository.save(crearFigura(
                                "Mario",
                                personajes.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(rojo.getId(), azul.getId()), 10, 5));

                figuraRepository.save(crearFigura(
                                "Luigi",
                                personajes.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(verde.getId(), azul.getId()), 10, 5));

                figuraRepository.save(crearFigura(
                                "Sonic",
                                personajes.getId(),
                                Dificultad.AVANZADO,
                                List.of(azul.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Papá Noel",
                                navidad.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(rojo.getId(), blanco.getId()), 15, 8));

                figuraRepository.save(crearFigura(
                                "Muñeco de Nieve",
                                navidad.getId(),
                                Dificultad.PRINCIPIANTE,
                                List.of(blanco.getId()), 12, 6));

                figuraRepository.save(crearFigura(
                                "Rudolf",
                                navidad.getId(),
                                Dificultad.INTERMEDIO,
                                List.of(marron.getId(), rojo.getId()), 15, 8));
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
                        List<String> coloresIds,
                        Integer altura,
                        Integer ancho) {

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
                figura.setAltura(altura);
                figura.setAncho(ancho);
                figura.setFechaCreacion(LocalDateTime.now());
                figura.setFechaModificacion(LocalDateTime.now());

                return figura;
        }
}
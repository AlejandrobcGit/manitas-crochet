package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.dto.ColorDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.FiguraNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FiguraService {

        private final FiguraRepository figuraRepository;
        private final CategoriaRepository categoriaRepository;
        private final ColorRepository colorRepository;

        // Obtener todas las figuras en formato DTO
        public List<FiguraListadoDto> obtenerTodasDto() {

                return figuraRepository.findAll()
                                .stream()
                                .map(this::convertirADto)
                                .toList();
        }

        // Convertir Figura a FiguraListadoDto
        private FiguraListadoDto convertirADto(Figura figura) {

                String categoria = categoriaRepository
                                .findById(figura.getCategoriaId())
                                .map(Categoria::getNombre)
                                .orElse("Sin categoría");

                List<ColorDto> colores = figura.getColoresIds()
                                .stream()
                                .map(colorId -> colorRepository.findById(colorId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(color -> new ColorDto(
                                                color.getNombre(),
                                                color.getCodigo()))
                                .toList();

                return new FiguraListadoDto(
                                figura.getId(),
                                figura.getNombre(),
                                figura.getDescripcion(),
                                categoria,
                                figura.getDificultad(),
                                figura.getAutor(),
                                figura.getImagenPrincipal(),
                                colores);
        }

        // Obtener figura por id
        public Figura obtenerPorId(String id) {

                return figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Crear figura
        public Figura guardar(Figura figura) {

                categoriaRepository.findById(figura.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figura.getCategoriaId()));

                for (String colorId : figura.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                figura.setFechaCreacion(LocalDateTime.now());
                figura.setFechaModificacion(LocalDateTime.now());

                return figuraRepository.save(figura);
        }

        // Actualizar figura
        public Figura actualizar(
                        String id,
                        Figura figuraActualizada) {

                Figura figura = figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                categoriaRepository.findById(
                                figuraActualizada.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figuraActualizada.getCategoriaId()));

                for (String colorId : figuraActualizada.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                figura.setNombre(figuraActualizada.getNombre());
                figura.setDescripcion(figuraActualizada.getDescripcion());
                figura.setCategoriaId(figuraActualizada.getCategoriaId());
                figura.setDificultad(figuraActualizada.getDificultad());
                figura.setAutor(figuraActualizada.getAutor());
                figura.setImagenPrincipal(figuraActualizada.getImagenPrincipal());
                figura.setColoresIds(figuraActualizada.getColoresIds());
                figura.setFechaModificacion(LocalDateTime.now());
                return figuraRepository.save(figura);
        }

        // Eliminar figura
        public void eliminar(String id) {

                figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                figuraRepository.deleteById(id);
        }
}
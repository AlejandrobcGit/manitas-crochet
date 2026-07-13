package com.manitascrochet.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

    // Obtener todas las figuras
    public List<Figura> obtenerTodas() {
        return figuraRepository.findAll();
    }

    // Obtener una figura por id
    public Optional<Figura> obtenerPorId(String id) {
        return figuraRepository.findById(id);
    }

    // Crear o guardar figura
    public Figura guardar(Figura figura) {

        categoriaRepository.findById(figura.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("La categoría no existe"));

        for (String colorId : figura.getColoresIds()) {

            colorRepository.findById(colorId)
                    .orElseThrow(() -> new RuntimeException(
                            "El color con id " + colorId + " no existe"));
        }
        return figuraRepository.save(figura);
    }

    // Actualizar figura
    public Figura actualizar(String id, Figura figuraActualizada) {

        Figura figura = figuraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Figura no encontrada"));

        categoriaRepository.findById(figura.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("La categoría no existe"));

        for (String colorId : figura.getColoresIds()) {

            colorRepository.findById(colorId)
                    .orElseThrow(() -> new RuntimeException(
                            "El color con id " + colorId + " no existe"));
        }
        
        figura.setNombre(figuraActualizada.getNombre());
        figura.setDescripcion(figuraActualizada.getDescripcion());
        figura.setCategoriaId(figuraActualizada.getCategoriaId());
        figura.setDificultad(figuraActualizada.getDificultad());
        figura.setAutor(figuraActualizada.getAutor());
        figura.setImagenPrincipal(figuraActualizada.getImagenPrincipal());
        figura.setColoresIds(figuraActualizada.getColoresIds());

        return figuraRepository.save(figura);
    }

    // Eliminar figura
    public void eliminar(String id) {
        figuraRepository.deleteById(id);
    }
}
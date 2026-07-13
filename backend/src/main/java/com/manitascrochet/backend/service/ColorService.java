package com.manitascrochet.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.repository.ColorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    // Obtener todas las colors
    public List<Color> obtenerTodas() {
        return colorRepository.findAll();
    }

    // Obtener una color por id
    public Optional<Color> obtenerPorId(String id) {
        return colorRepository.findById(id);
    }

    // Crear o guardar color
    public Color guardar(Color color) {

        colorRepository.findByNombreIgnoreCase(color.getNombre())
                .ifPresent(existingColor -> {
                    throw new RuntimeException(
                            "Ya existe un color con el mismo nombre");
                });

        colorRepository.findByCodigoIgnoreCase(color.getCodigo())
                .ifPresent(existingColor -> {
                    throw new RuntimeException(
                            "Ya existe un color con el mismo código");
                });

        return colorRepository.save(color);
    }

    // Actualizar color
    public Color actualizar(String id, Color colorActualizada) {

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Color no encontrada"));

        colorRepository.findByCodigoIgnoreCase(colorActualizada.getCodigo())
                .ifPresent(existingColor -> {
                    if (!existingColor.getId().equals(id)) {
                        throw new RuntimeException("Ya existe una color con el mismo código");
                    }
                });

        color.setNombre(colorActualizada.getNombre());
        color.setCodigo(colorActualizada.getCodigo());

        return colorRepository.save(color);
    }

    // Eliminar color
    public void eliminar(String id) {
        colorRepository.deleteById(id);
    }
}
package com.manitascrochet.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.exception.GlobalExceptionHandler.CodigoColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.repository.ColorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;

    // Obtener todos los colores
    public List<Color> obtenerTodos() {
        return colorRepository.findAll();
    }

    // Obtener color por id
    public Color obtenerPorId(String id) {
        return colorRepository.findById(id).orElseThrow(() -> new ColorNoEncontradoException(id));
    }

    // Crear color
    public Color guardar(Color color) {

        colorRepository.findByNombreIgnoreCase(
                color.getNombre())
                .ifPresent(existingColor -> {
                    throw new ColorDuplicadoException(
                            color.getNombre());
                });

        colorRepository.findByCodigoIgnoreCase(
                color.getCodigo())
                .ifPresent(existingColor -> {
                    throw new CodigoColorDuplicadoException(
                            color.getCodigo());
                });

        return colorRepository.save(color);
    }

    // Actualizar color
    public Color actualizar(
            String id,
            Color colorActualizado) {

        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ColorNoEncontradoException(id));

        colorRepository.findByNombreIgnoreCase(
                colorActualizado.getNombre())
                .ifPresent(existingColor -> {

                    if (!existingColor.getId().equals(id)) {
                        throw new ColorDuplicadoException(
                                colorActualizado.getNombre());
                    }
                });

        colorRepository.findByCodigoIgnoreCase(
                colorActualizado.getCodigo())
                .ifPresent(existingColor -> {

                    if (!existingColor.getId().equals(id)) {
                        throw new CodigoColorDuplicadoException(
                                colorActualizado.getCodigo());
                    }
                });

        color.setNombre(colorActualizado.getNombre());
        color.setCodigo(colorActualizado.getCodigo());

        return colorRepository.save(color);
    }

    // Eliminar color
    public void eliminar(String id) {

        colorRepository.findById(id)
                .orElseThrow(() -> new ColorNoEncontradoException(id));

        colorRepository.deleteById(id);
    }
}
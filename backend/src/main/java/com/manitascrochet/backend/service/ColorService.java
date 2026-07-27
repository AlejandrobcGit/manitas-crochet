package com.manitascrochet.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.manitascrochet.backend.exception.GlobalExceptionHandler1.CodigoColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler1.ColorDuplicadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler1.ColorNoEncontradoException;
import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.repository.ColorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;
    /*
     * permite trabajar directamente con MongoDB sin pasar por un repositorio
     * (MongoRepository).
     * Es la implementación principal de la interfaz MongoOperations y proporciona
     * operaciones para crear, consultar, actualizar y borrar documentos.
     */

    private final MongoTemplate mongoTemplate;

    // Obtener todos los colores
    public List<Color> obtenerTodos(String nombre, String codigo) {
        Query query = new Query();
        List<Criteria> criterios = new ArrayList<>();

        if (nombre != null && !nombre.isBlank()) {
            criterios.add(Criteria.where("nombre").regex(nombre, "i")); // buscador → parcial
        }

        if (codigo != null && !codigo.isBlank()) {
            criterios.add(Criteria.where("codigo").regex(codigo, "i")); // buscador → parcial
        }
    // Combina todos los filtros antes de ejecutar la consulta.    
        if (!criterios.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criterios.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, Color.class);

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
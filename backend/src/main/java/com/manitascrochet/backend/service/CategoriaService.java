package com.manitascrochet.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaDuplicadaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // Obtener todas las categorias
    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    // Obtener una categoria por id
    public Categoria obtenerPorId(String id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
    }

    // Crear o guardar categoria
    public Categoria guardar(Categoria categoria) {
        categoriaRepository.findByNombreIgnoreCase(categoria.getNombre())
                .ifPresent(existingCategoria -> {
                    throw new CategoriaDuplicadaException(categoria.getNombre());
                });
        return categoriaRepository.save(categoria);
    }

    // Actualizar categoria
    public Categoria actualizar(String id, Categoria categoriaActualizada) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));

        categoriaRepository.findByNombreIgnoreCase(
                categoriaActualizada.getNombre())
                .ifPresent(existingCategoria -> {

                    if (!existingCategoria.getId().equals(id)) {
                        throw new CategoriaDuplicadaException(
                                categoriaActualizada.getNombre());
                    }
                });

        categoria.setNombre(
                categoriaActualizada.getNombre());

        return categoriaRepository.save(categoria);
    }

    // Eliminar categoria
    public void eliminar(String id) {
        categoriaRepository.findById(id)
                .orElseThrow(() -> new CategoriaNoEncontradaException(id));
        categoriaRepository.deleteById(id);
    }
}
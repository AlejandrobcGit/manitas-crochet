package com.manitascrochet.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
    public Optional<Categoria> obtenerPorId(String id) {
        return categoriaRepository.findById(id);
    }

    // Crear o guardar categoria
    public Categoria guardar(Categoria categoria) {
         categoriaRepository.findByNombreIgnoreCase(categoria.getNombre())
                .ifPresent(existingCategoria -> {
                    throw new RuntimeException(
                            "Ya existe una categoria con el mismo nombre");
                });
        return categoriaRepository.save(categoria);
    }

    // Actualizar categoria
    public Categoria actualizar(String id, Categoria categoriaActualizada) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
        
        categoriaRepository.findByNombreIgnoreCase(categoriaActualizada.getNombre())
                .ifPresent(existingCategoria -> {
                    if (!existingCategoria.getId().equals(id)) {
                        throw new RuntimeException("Ya existe una categoria con el mismo nombre");
                    }
                });

        categoria.setNombre(categoriaActualizada.getNombre());
        
        return categoriaRepository.save(categoria);
    }

    // Eliminar categoria
    public void eliminar(String id) {
        categoriaRepository.deleteById(id);
    }
}
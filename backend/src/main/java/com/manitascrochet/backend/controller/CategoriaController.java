package com.manitascrochet.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // GET /api/categorias
    @GetMapping
    public List<Categoria> obtenerTodas() {
        return categoriaService.obtenerTodas();
    }

    // GET /api/categorias/{id}
    @GetMapping("/{id}")
    public Categoria obtenerPorId(@PathVariable String id) {
        return categoriaService.obtenerPorId(id);
    }

    // POST /api/categorias
    @PostMapping
    public Categoria crear(@RequestBody Categoria categoria) {
        return categoriaService.guardar(categoria);
    }

    // PUT /api/categorias/{id}
    @PutMapping("/{id}")
    public Categoria actualizar(
            @PathVariable String id,
            @RequestBody Categoria categoria) {

        return categoriaService.actualizar(id, categoria);
    }

    // DELETE /api/categorias/{id}
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        categoriaService.eliminar(id);
    }
}
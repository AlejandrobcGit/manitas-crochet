package com.manitascrochet.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.model.Color;
import com.manitascrochet.backend.service.ColorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/color")
@RequiredArgsConstructor
public class ColorController {

    private final ColorService colorService;

    // GET /api/color
    @GetMapping
    public List<Color> obtenerTodas() {
        return colorService.obtenerTodas();
    }

    // GET /api/color/{id}
    @GetMapping("/{id}")
    public Optional<Color> obtenerPorId(@PathVariable String id) {
        return colorService.obtenerPorId(id);
    }

    // POST /api/color
    @PostMapping
    public Color crear(@RequestBody Color color) {
        return colorService.guardar(color);
    }

    // PUT /api/color/{id}
    @PutMapping("/{id}")
    public Color actualizar(
            @PathVariable String id,
            @RequestBody Color color) {

        return colorService.actualizar(id, color);
    }

    // DELETE /api/color/{id}
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        colorService.eliminar(id);
    }
}
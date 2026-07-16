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

import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.service.FiguraService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/figuras")
@RequiredArgsConstructor
public class FiguraController {

    private final FiguraService figuraService;

    // GET /api/figuras
    @GetMapping
    public List<FiguraListadoDto> obtenerTodas() {
        return figuraService.obtenerTodasDto();
    }

    // GET /api/figuras/{id}
    @GetMapping("/{id}")
    public Optional<Figura> obtenerPorId(@PathVariable String id) {
        return figuraService.obtenerPorId(id);
    }

    // POST /api/figuras
    @PostMapping
    public Figura crear(@RequestBody Figura figura) {
        return figuraService.guardar(figura);
    }

    // PUT /api/figuras/{id}
    @PutMapping("/{id}")
    public Figura actualizar(
            @PathVariable String id,
            @RequestBody Figura figura) {

        return figuraService.actualizar(id, figura);
    }

    // DELETE /api/figuras/{id}
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        figuraService.eliminar(id);
    }
}
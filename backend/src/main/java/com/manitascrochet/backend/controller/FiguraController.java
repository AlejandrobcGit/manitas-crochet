package com.manitascrochet.backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.service.FiguraService;

import jakarta.validation.Valid;
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
    public FiguraDetalleDto obtenerPorId(@PathVariable String id) {
        return figuraService.obtenerPorIdDto(id);
    }

    // POST /api/figuras
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FiguraDetalleDto crearFigura(

            @RequestPart("data") @Valid Figura figura,

            @RequestPart("imagenPrincipal") MultipartFile imagenPrincipal,

            @RequestPart(value = "imagenesSecundarias", required = false) List<MultipartFile> imagenesSecundarias) {

        return figuraService.crear(
                figura,
                imagenPrincipal,
                imagenesSecundarias);

    }

    // PUT /api/figuras/{id}
    @PutMapping("/{id}")
    public FiguraDetalleDto actualizar(
            @PathVariable String id,

            @RequestPart("data") @Valid Figura figura,

            @RequestPart(value = "imagenPrincipal", required = false) MultipartFile imagenPrincipal,

            @RequestPart(value = "imagenesSecundarias", required = false) List<MultipartFile> imagenesSecundarias) {

        return figuraService.actualizar(id, figura,
                imagenPrincipal,
                imagenesSecundarias);
    }

    // DELETE /api/figuras/{id}
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        figuraService.eliminar(id);
    }
}
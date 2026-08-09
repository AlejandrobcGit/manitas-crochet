package com.manitascrochet.backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.dto.FiguraRequestDto;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.service.FiguraService;
import com.manitascrochet.backend.service.VisualizacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/figuras")
@RequiredArgsConstructor
public class FiguraController {

    private final FiguraService figuraService;
    private final VisualizacionService visualizacionService;


    // GET /api/figuras
    @GetMapping
    public List<FiguraListadoDto> obtenerTodas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoriaId) {
        return figuraService.obtenerTodasDto(nombre, categoriaId);
    }

    // GET /api/figuras/{id}
    @GetMapping("/{id}")
    public FiguraDetalleDto obtenerPorId(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FiguraDetalleDto figuraDto = figuraService.obtenerPorIdDto(id, userDetails);
        
        // Si no hay usuario logado, userDetails llega como null (no hay 401,
        // porque el endpoint es público) — el service debe manejarlo
        visualizacionService.marcarVisualizacion(id, userDetails);

        return figuraDto;

    }

    // POST /api/figuras
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FiguraDetalleDto crearFigura(

            @RequestPart("data") @Valid FiguraRequestDto figuraRequestDto,

            @RequestPart("imagenPrincipal") MultipartFile imagenPrincipal,

            @RequestPart(value = "imagenesSecundarias", required = false) List<MultipartFile> imagenesSecundarias) {

        Figura figura = new Figura();
        figura.setNombre(figuraRequestDto.getNombre());
        figura.setDescripcion(figuraRequestDto.getDescripcion());   
        figura.setCategoriaId(figuraRequestDto.getCategoriaId());
        figura.setDificultad(figuraRequestDto.getDificultad());
        figura.setAutor(figuraRequestDto.getAutor());
        figura.setColoresIds(figuraRequestDto.getColoresIds());
        figura.setAltura(figuraRequestDto.getAltura());
        figura.setAncho(figuraRequestDto.getAncho());
        figura.setPeso(figuraRequestDto.getPeso());

        return figuraService.crear(
                figura,
                imagenPrincipal,
                imagenesSecundarias);

    }

    // PUT /api/figuras/{id}
    @PutMapping("/{id}")
    public FiguraDetalleDto actualizar(
            @PathVariable String id,

            @RequestPart("data")@Valid FiguraRequestDto figuraRequestDto,

            @RequestPart(value = "imagenPrincipal", required = false) MultipartFile imagenPrincipal,

            @RequestPart(value = "imagenesSecundarias", required = false) List<MultipartFile> imagenesSecundarias) {

        Figura figura = new Figura();
        figura.setNombre(figuraRequestDto.getNombre());
        figura.setDescripcion(figuraRequestDto.getDescripcion());
        figura.setCategoriaId(figuraRequestDto.getCategoriaId());
        figura.setDificultad(figuraRequestDto.getDificultad());
        figura.setAutor(figuraRequestDto.getAutor());
        figura.setColoresIds(figuraRequestDto.getColoresIds());
        figura.setAltura(figuraRequestDto.getAltura());
        figura.setAncho(figuraRequestDto.getAncho());
        figura.setPeso(figuraRequestDto.getPeso());

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
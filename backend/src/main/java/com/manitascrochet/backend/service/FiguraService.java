package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.manitascrochet.backend.dto.ColorDto;
import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.FiguraNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FiguraService {

        private final FiguraRepository figuraRepository;
        private final CategoriaRepository categoriaRepository;
        private final ColorRepository colorRepository;
        private final FileStorageService fileStorageService;

        // Obtener todas las figuras en formato DTO
        public List<FiguraListadoDto> obtenerTodasDto() {

                return figuraRepository.findAll()
                                .stream()
                                .map(this::convertirFiguraListadoDto)
                                .toList();
        }

        // Convertir Figura a FiguraListadoDto
        private FiguraListadoDto convertirFiguraListadoDto(Figura figura) {

                String categoria = categoriaRepository
                                .findById(figura.getCategoriaId())
                                .map(Categoria::getNombre)
                                .orElseThrow(() -> new CategoriaNoEncontradaException(figura.getCategoriaId()));

                return new FiguraListadoDto(
                                figura.getId(),
                                figura.getNombre(),
                                categoria,
                                figura.getImagenPrincipal(),
                                figura.getAltura(),
                                figura.getAncho());
        }

        // Obtener figura por id
        public Figura obtenerPorId(String id) {
                return figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Obtener figura por id en formato DTO
        public FiguraDetalleDto obtenerPorIdDto(String id) {

                return convertirFiguraDetalleDto(
                                figuraRepository.findById(id)
                                                .orElseThrow(() -> new FiguraNoEncontradaException(id)));
        }

        // Convertir Figura a FiguraDetalleDto
        private FiguraDetalleDto convertirFiguraDetalleDto(Figura figura) {

                String categoria = categoriaRepository
                                .findById(figura.getCategoriaId())
                                .map(Categoria::getNombre)
                                .orElseThrow(() -> new CategoriaNoEncontradaException(figura.getCategoriaId()));

                List<ColorDto> colores = figura.getColoresIds()
                                .stream()
                                .map(colorId -> colorRepository.findById(colorId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(color -> new ColorDto(
                                                color.getNombre(),
                                                color.getCodigo()))
                                .toList();

                return new FiguraDetalleDto(
                                figura.getId(),
                                figura.getNombre(),
                                figura.getDescripcion(),
                                categoria,
                                figura.getDificultad(),
                                figura.getAutor(),
                                figura.getImagenPrincipal(),
                                figura.getImagenesSecundarias(),
                                colores,
                                figura.getAltura(),
                                figura.getAncho());
        }

        // Crear figura
        public FiguraDetalleDto crear(Figura figura, MultipartFile imagenPrincipal,
                        List<MultipartFile> imagenesSecundarias) {

                categoriaRepository.findById(figura.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figura.getCategoriaId()));

                for (String colorId : figura.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                figura.setFechaCreacion(LocalDateTime.now());
                figura.setFechaModificacion(LocalDateTime.now());

                // Guardamos primero SIN imágenes para que Mongo genere el ID.
                Figura figuraGuardada = figuraRepository.save(figura);

                // Guardar imagen principal
                if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {

                        String filename = fileStorageService.store(
                                        imagenPrincipal,
                                        figuraGuardada.getId(),
                                        figuraGuardada.getNombre());

                        figuraGuardada.setImagenPrincipal(filename);
                }

                // Guardar imágenes secundarias, cada una con un sufijo -1, -2, -3...
                if (imagenesSecundarias != null && !imagenesSecundarias.isEmpty()) {

                        List<String> nombresImagenes = new ArrayList<>();
                        int indice = 1;

                        for (MultipartFile imagen : imagenesSecundarias) {

                                if (!imagen.isEmpty()) {

                                        String nombreDiferenciado = figuraGuardada.getNombre() + "-" + indice;

                                        String filename = fileStorageService.store(
                                                        imagen,
                                                        figuraGuardada.getId(),
                                                        nombreDiferenciado);

                                        nombresImagenes.add(filename);
                                        indice++;
                                }
                        }

                        figuraGuardada.setImagenesSecundarias(nombresImagenes);
                }

                // Segundo save: ahora sí con los nombres de archivo ya calculados.
                return convertirFiguraDetalleDto(figuraRepository.save(figuraGuardada));
        }

        // Actualizar figura
        public Figura actualizar(
                        String id,
                        Figura figuraActualizada) {

                Figura figura = figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                categoriaRepository.findById(
                                figuraActualizada.getCategoriaId())
                                .orElseThrow(() -> new CategoriaNoEncontradaException(
                                                figuraActualizada.getCategoriaId()));

                for (String colorId : figuraActualizada.getColoresIds()) {

                        colorRepository.findById(colorId)
                                        .orElseThrow(() -> new ColorNoEncontradoException(
                                                        colorId));
                }

                figura.setNombre(figuraActualizada.getNombre());
                figura.setDescripcion(figuraActualizada.getDescripcion());
                figura.setCategoriaId(figuraActualizada.getCategoriaId());
                figura.setDificultad(figuraActualizada.getDificultad());
                figura.setAutor(figuraActualizada.getAutor());
                figura.setImagenPrincipal(figuraActualizada.getImagenPrincipal());
                figura.setColoresIds(figuraActualizada.getColoresIds());
                figura.setFechaModificacion(LocalDateTime.now());
                return figuraRepository.save(figura);
        }

        // Eliminar figura
        public void eliminar(String id) {

                figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                figuraRepository.deleteById(id);
        }
}
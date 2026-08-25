package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.manitascrochet.backend.dto.ColorResponseDto;
import com.manitascrochet.backend.dto.FiguraDetalleDto;
import com.manitascrochet.backend.dto.FiguraListadoDto;
import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.dto.ResumenValoracionDto;
import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.CategoriaNoEncontradaException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ColorNoEncontradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.FiguraNoEncontradaException;
import com.manitascrochet.backend.model.Categoria;
import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.repository.CategoriaRepository;
import com.manitascrochet.backend.repository.ColorRepository;
import com.manitascrochet.backend.repository.FiguraRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FiguraService {

        private final FiguraRepository figuraRepository;
        private final CategoriaRepository categoriaRepository;
        private final ColorRepository colorRepository;
        private final ImageService imageService;
        // si elimnara el FileStorageService, cuando imageUpload esta lito
        // private final FileStorageService fileStorageService;
        private final ValoracionService valoracionService;
        private final ComentarioService comentarioService;
        @Value("${imagekit.url.endpoint}")
        private String imageUrl;
        @Value("${imagekit.folder}")
        private String imageFolder;

        /*
         * permite trabajar directamente con MongoDB sin pasar por un repositorio
         * (MongoRepository).
         * Es la implementación principal de la interfaz MongoOperations y proporciona
         * operaciones para crear, consultar, actualizar y borrar documentos.
         */

        private final MongoTemplate mongoTemplate;

        // Obtener todas las figuras en formato DTO
        public List<FiguraListadoDto> obtenerTodasDto(String nombre, String categoriaId) {

                Query query = new Query();
                List<Criteria> criterios = new ArrayList<>();

                if (nombre != null && !nombre.isBlank()) {
                        criterios.add(Criteria.where("nombre").regex(nombre, "i")); // buscador → parcial
                }

                if (categoriaId != null && !categoriaId.isBlank()) {
                        criterios.add(Criteria.where("categoriaId").is(categoriaId)); // filtro → exacto
                }
                // Combina todos los filtros antes de ejecutar la consulta.
                if (!criterios.isEmpty()) {
                        query.addCriteria(new Criteria().andOperator(criterios.toArray(Criteria[]::new)));
                }

                return mongoTemplate.find(query, Figura.class)
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

                if (figura.getImagenPrincipal() == null || figura.getImagenPrincipal().isBlank()) {
                        figura.setImagenPrincipal(imageUrl + "/" + imageFolder + "/default.webp");
                }

                ResumenValoracionDto resumenValoracionDto = valoracionService
                                .obtenerResumenValoraciones(figura.getId());

                return new FiguraListadoDto(
                                figura.getId(),
                                figura.getNombre(),
                                categoria,
                                figura.getImagenPrincipal(),
                                figura.getAltura(),
                                figura.getAncho(),
                                resumenValoracionDto.getValoracionMedia(),
                                resumenValoracionDto.getTotalValoraciones());
        }

        // Obtener figura por id
        public Figura obtenerPorId(String id) {
                return figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Obtener figura por id en formato DTO
        public FiguraDetalleDto obtenerPorIdDto(String id, UserDetailsImpl userDetails) {

                return figuraRepository.findById(id)
                                .map(figura -> convertirFiguraDetalleDto(figura, userDetails))
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));
        }

        // Convertir Figura a FiguraDetalleDto
        private FiguraDetalleDto convertirFiguraDetalleDto(Figura figura, UserDetailsImpl userDetails) {

                String categoria = categoriaRepository
                                .findById(figura.getCategoriaId())
                                .map(Categoria::getNombre)
                                .orElseThrow(() -> new CategoriaNoEncontradaException(figura.getCategoriaId()));

                List<ColorResponseDto> colores = figura.getColoresIds()
                                .stream()
                                .map(colorId -> colorRepository.findById(colorId))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(color -> new ColorResponseDto(
                                                color.getNombre(),
                                                color.getCodigo()))
                                .toList();

                if (figura.getImagenPrincipal() == null || figura.getImagenPrincipal().isBlank()) {
                        figura.setImagenPrincipal("https://ik.imagekit.io/8hlhxb9hx/manitas-Crochet/default.webp");
                }

                ResumenValoracionDto resumenValoracionDto = valoracionService
                                .obtenerResumenValoraciones(figura.getId());

                ValoracionDto valoracionUsuario = (userDetails == null)
                                ? new ValoracionDto(0)
                                : valoracionService.obtenerValoracionUsuario(userDetails.getId(), figura.getId());
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
                                figura.getAncho(),
                                figura.getPeso(),
                                resumenValoracionDto.getValoracionMedia(),
                                valoracionUsuario.getPuntuacion(),
                                resumenValoracionDto.getTotalValoraciones());
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

                        /*
                         * String filename = fileStorageService.store(
                         * imagenPrincipal,
                         * figuraGuardada.getId(),
                         * figuraGuardada.getNombre());
                         */

                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                        figuraGuardada.getId(),
                                        figuraGuardada.getNombre(),
                                        imagenPrincipal);

                        figuraGuardada.setImagenPrincipal(imageUploadResultDto.getUrl());
                        figuraGuardada.setFileId_imagenPrincipal(imageUploadResultDto.getFileId());
                }

                // Guardar imágenes secundarias, cada una con un sufijo -1, -2, -3...
                if (imagenesSecundarias != null && !imagenesSecundarias.isEmpty()) {

                        List<String> nombresImagenes = new ArrayList<>();
                        List<String> fileIdImagenes = new ArrayList<>();
                        int indice = 1;

                        for (MultipartFile imagen : imagenesSecundarias) {

                                if (!imagen.isEmpty()) {

                                        String nombreDiferenciado = figuraGuardada.getNombre() + "-" + indice;

                                        /*
                                         * String filename = fileStorageService.store(
                                         * imagen,
                                         * figuraGuardada.getId(),
                                         * nombreDiferenciado);
                                         */
                                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                                        figuraGuardada.getId(),
                                                        nombreDiferenciado,
                                                        imagen);

                                        nombresImagenes.add(imageUploadResultDto.getUrl());
                                        fileIdImagenes.add(imageUploadResultDto.getFileId());
                                        indice++;
                                }
                        }

                        figuraGuardada.setImagenesSecundarias(nombresImagenes);
                        figuraGuardada.setFileId_imagenesSecundarias(fileIdImagenes);

                }

                // Segundo save: ahora sí con los nombres de archivo ya calculados.
                return convertirFiguraDetalleDto(figuraRepository.save(figuraGuardada), null);
        }

        // Actualizar figura
        public FiguraDetalleDto actualizar(
                        String id,
                        Figura figuraActualizada,
                        MultipartFile imagenPrincipal,
                        List<MultipartFile> imagenesSecundarias) {

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

                // ----------------------------------------------------
                // DATOS BÁSICOS
                // ----------------------------------------------------

                figura.setNombre(figuraActualizada.getNombre());
                figura.setDescripcion(figuraActualizada.getDescripcion());
                figura.setCategoriaId(figuraActualizada.getCategoriaId());
                figura.setDificultad(figuraActualizada.getDificultad());
                figura.setAltura(figuraActualizada.getAltura());
                figura.setAncho(figuraActualizada.getAncho());
                figura.setPeso(figuraActualizada.getPeso());
                figura.setAutor(figuraActualizada.getAutor());
                figura.setColoresIds(figuraActualizada.getColoresIds());

                // ----------------------------------------------------
                // IMAGEN PRINCIPAL
                // ----------------------------------------------------

                if (imagenPrincipal != null && !imagenPrincipal.isEmpty()) {

                        // Guardar nuevo y borrar anterior
                        String previousFileId = figura.getFileId_imagenPrincipal();

                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                        figura.getId(),
                                        figura.getNombre(),
                                        imagenPrincipal);

                        figura.setImagenPrincipal(imageUploadResultDto.getUrl());
                        figura.setFileId_imagenPrincipal(imageUploadResultDto.getFileId());

                        if (previousFileId != null && !previousFileId.isBlank()) {
                                imageService.deleteImage(previousFileId);
                        }
                }

                // ----------------------------------------------------
                // IMÁGENES SECUNDARIAS
                // ----------------------------------------------------

                if (imagenesSecundarias != null && !imagenesSecundarias.isEmpty()) {

                        List<String> previousFileIds = figura.getFileId_imagenesSecundarias() == null
                                        ? new ArrayList<>()
                                        : new ArrayList<>(figura.getFileId_imagenesSecundarias());

                        List<String> nombresImagenes = new ArrayList<>();
                        List<String> fileIdImagenes = new ArrayList<>();
                        int indice = 1;

                        for (MultipartFile imagen : imagenesSecundarias) {

                                if (!imagen.isEmpty()) {

                                        String nombreDiferenciado = figura.getNombre() + "-" + indice;

                                        ImageUploadResultDto imageUploadResultDto = imageService.uploadImage(
                                                        figura.getId(),
                                                        nombreDiferenciado,
                                                        imagen);

                                        nombresImagenes.add(imageUploadResultDto.getUrl());
                                        fileIdImagenes.add(imageUploadResultDto.getFileId());
                                        indice++;
                                }
                        }

                        figura.setImagenesSecundarias(nombresImagenes);
                        figura.setFileId_imagenesSecundarias(fileIdImagenes);

                        // Borrar imágenes secundarias anteriores
                        for (String imagenId : previousFileIds) {
                                imageService.deleteImage(imagenId);
                        }

                }

                // ----------------------------------------------------
                // FECHA MODIFICACIÓN
                // ----------------------------------------------------

                figura.setFechaModificacion(LocalDateTime.now());

                return convertirFiguraDetalleDto(figuraRepository.save(figura), null);
        }

        // Eliminar figura
        public void eliminar(String id) {

                Figura figura = figuraRepository.findById(id)
                                .orElseThrow(() -> new FiguraNoEncontradaException(id));

                // 1. Borrar imagen principal de ImageKit
                if (figura.getFileId_imagenPrincipal() != null
                                && !figura.getFileId_imagenPrincipal().isBlank()) {
                        imageService.deleteImage(figura.getFileId_imagenPrincipal());
                }

                // 2. Borrar imágenes secundarias de ImageKit
                if (figura.getFileId_imagenesSecundarias() != null) {
                        for (String fileId : figura.getFileId_imagenesSecundarias()) {
                                if (fileId != null && !fileId.isBlank()) {
                                        imageService.deleteImage(fileId);
                                }
                        }
                }

                // 3. Borrar datos relacionados
                valoracionService.eliminarValoracionesPorFigura(id);
                comentarioService.eliminarComentariosPorFigura(id);

                // 4. Borrar figura
                figuraRepository.deleteById(id);
        }
}
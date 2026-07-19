package com.manitascrochet.backend.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.service.FileStorageService;
import com.manitascrochet.backend.service.FiguraService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * Controlador encargado de gestionar las imágenes asociadas
 * a las figuras del catálogo.
 *
 * Funcionalidades:
 * - Subir imagen principal de una figura.
 * - Obtener una imagen por nombre de fichero.
 * - Obtener la URL pública de una imagen.
 * - Eliminar una imagen asociada.
 *
 * Ruta base:
 * /api/imagenes
 */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class FileStorageController {

        private final FileStorageService fileStorageService;
        private final FiguraService figuraService;

        // ---------------------------------------------------------
        // 1. SUBIR IMAGEN
        // ---------------------------------------------------------

        @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<String> uploadImg(
                        @PathVariable String id,
                        @RequestPart("file") @NotNull MultipartFile file) {

                Figura figura = figuraService.obtenerPorId(id);

                String storedFilename = fileStorageService.store(file, id, figura.getNombre());

                figura.setImagenPrincipal(storedFilename);

                return ResponseEntity.ok(
                                "Imagen subida correctamente: "
                                                + storedFilename);
        }

        // ---------------------------------------------------------
        // 2. OBTENER IMAGEN
        // ---------------------------------------------------------

        @GetMapping("/{filename:.+}")
        public ResponseEntity<Resource> getImage(
                        @PathVariable String filename) {

                if (filename == null ||
                                filename.isBlank() ||
                                filename.equals("null")) {

                        return ResponseEntity.notFound().build();
                }

                filename = URLDecoder.decode(
                                filename,
                                StandardCharsets.UTF_8);

                Resource resource = fileStorageService.loadAsResource(filename);

                return ResponseEntity.ok()
                                .cacheControl(
                                                CacheControl.maxAge(
                                                                30,
                                                                TimeUnit.DAYS))
                                .contentType(MediaType.IMAGE_JPEG)
                                .body(resource);
        }

        // ---------------------------------------------------------
        // 3. OBTENER URL DE LA IMAGEN
        // ---------------------------------------------------------

        @GetMapping("/url/{id}")
        public ResponseEntity<String> getImgUrl(
                        @PathVariable String id) {

                Figura figura = figuraService.obtenerPorId(id);

                String filename = figura.getImagenPrincipal();

                if (filename == null ||
                                filename.isBlank()) {

                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "La figura no tiene imagen asociada");
                }

                String url = fileStorageService.getFileUrl(filename);

                return ResponseEntity.ok(url);
        }

        // ---------------------------------------------------------
        // 4. ELIMINAR IMAGEN
        // ---------------------------------------------------------

        @DeleteMapping("/{id}")
        public ResponseEntity<String> deleteImg(
                        @PathVariable String id) {

                Figura figura = figuraService.obtenerPorId(id);

                String filename = figura.getImagenPrincipal();

                if (filename == null ||
                                filename.isBlank()) {

                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "La figura no tiene imagen asociada");
                }

                fileStorageService.delete(filename);

                figura.setImagenPrincipal(null);

                return ResponseEntity.ok(
                                "Imagen eliminada correctamente");
        }
}
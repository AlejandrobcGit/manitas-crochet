package com.manitascrochet.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploadDir");

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de subida", e);
        }
    }

    public String store(MultipartFile file, String id, String titulo) {

        if (file.isEmpty()) {
            throw new RuntimeException("Fichero vacío");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new RuntimeException("Solo se permiten imágenes");
        }

        // Obtener extensión
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new RuntimeException("Archivo sin extensión");
        }

        // Crear nombre seguro
        String safeTitle = titulo.replaceAll("[^a-zA-Z0-9-_]", "_");
        String storedFilename = id + "_" + safeTitle + "." + extension;

        try (InputStream inputStream = file.getInputStream()) {

            Files.copy(
                    inputStream,
                    this.rootLocation.resolve(storedFilename),
                    StandardCopyOption.REPLACE_EXISTING);

            return storedFilename;

        } catch (IOException ioe) {
            throw new RuntimeException("Error al guardar el archivo", ioe);
        }
    }

    public void delete(String filename) {
        if (filename != null) {
            try {
                Files.deleteIfExists(rootLocation.resolve(filename));
            } catch (IOException e) {
                System.out.println("Error al borrar archivo: " + e.getMessage());
            }
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException("No se pudo leer el archivo: " + filename);

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar archivo: " + filename, e);
        }
    }

    /* este devuelve localhost:9000/img/filename
    
     * public String getFileUrl(String filename) {
     * if (filename == null || filename.isBlank()) {
     * return "";
     * }
     * 
     * 
     * return ServletUriComponentsBuilder.fromCurrentContextPath()
     * .path("/img/")
     * .path(filename)
     * .toUriString();
     * 
     * return ServletUriComponentsBuilder
     * .fromRequestUri(request)
     * .replacePath("/img/" + filename)
     * .toUriString();
     * 
     * }
     */

    @Autowired
    private HttpServletRequest request;

    public String getFileUrl(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }

        if (request == null || request.getRequestURL() == null) {
            return "";
        }

        final HttpServletRequest request2 = request;
        
        if (request2 != null) {
            return ServletUriComponentsBuilder
                    .fromRequestUri(request2)
                    .replacePath("/img/" + filename)
                    .toUriString();
        } else {
            return null;
        }
    }

}

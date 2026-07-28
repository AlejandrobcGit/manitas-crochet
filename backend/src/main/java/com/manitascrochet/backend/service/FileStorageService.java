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

    private final Path rootLocation = Paths.get("uploadDir")
            .toAbsolutePath()
            .normalize();

    @Autowired
    private HttpServletRequest request;

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo crear la carpeta de subida", e);
        }
    }

    /**
     * Garantiza que siempre trabajemos dentro de uploadDir
     */
    private Path resolveSafePath(String filename) {

        Path resolvedPath = rootLocation
                .resolve(filename)
                .normalize();

        if (!resolvedPath.startsWith(rootLocation)) {
            throw new RuntimeException("Ruta no permitida");
        }

        return resolvedPath;
    }

    public String store(
            MultipartFile file,
            String id,
            String titulo) {

        if (file.isEmpty()) {
            throw new RuntimeException("Fichero vacío");
        }

        if (!Objects.requireNonNull(file.getContentType())
                .startsWith("image/")) {
            throw new RuntimeException(
                    "Solo se permiten imágenes");
        }

        String extension =
                StringUtils.getFilenameExtension(
                        file.getOriginalFilename());

        if (extension == null || extension.isBlank()) {
            throw new RuntimeException(
                    "Archivo sin extensión");
        }

        extension = extension.toLowerCase();

        if (!extension.matches("jpg|jpeg|png|webp")) {
            throw new RuntimeException(
                    "Formato de imagen no permitido");
        }

        String safeTitle =
                titulo.replaceAll("[^a-zA-Z0-9-_]", "_");

        String storedFilename =
                id + "_" + safeTitle + "." + extension;

        Path targetFile = resolveSafePath(storedFilename);

        try (InputStream inputStream =
                     file.getInputStream()) {

            Files.copy(
                    inputStream,
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING);

            return storedFilename;

        } catch (IOException ioe) {
            throw new RuntimeException(
                    "Error al guardar el archivo",
                    ioe);
        }
    }

    public void delete(String filename) {

        if (filename == null || filename.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(
                    resolveSafePath(filename));
        } catch (IOException e) {
            System.out.println(
                    "Error al borrar archivo: "
                            + e.getMessage());
        }
    }

    public Resource loadAsResource(String filename) {

        try {

            Path file =
                    resolveSafePath(filename);

            Resource resource =
                    new UrlResource(file.toUri());

            if (resource.exists()
                    && resource.isReadable()) {
                return resource;
            }

            throw new RuntimeException(
                    "No se pudo leer el archivo: "
                            + filename);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error al cargar archivo: "
                            + filename,
                    e);
        }
    }

    public String getFileUrl(String filename) {

        if (filename == null
                || filename.isBlank()) {
            return "";
        }

        if (request == null
                || request.getRequestURL() == null) {
            return "";
        }

        return ServletUriComponentsBuilder
                .fromRequestUri(request)
                .replacePath("/img/" + filename)
                .toUriString();
    }
}
package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;

import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageDeleteException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageUploadException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.InvalidImageException;

import io.imagekit.client.ImageKitClient;
import io.imagekit.errors.ImageKitException;
import io.imagekit.models.files.FileDeleteParams;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageKitClient imageKitClient;
    private final ImageCompressionService compressionService;

    /**
     * Valida, comprime a WebP y sube la imagen a ImageKit.
     * /**
     * Valida, comprime a WebP y sube la imagen a ImageKit.
     *
     * @param file   imagen recibida
     * @param folder carpeta destino en ImageKit
     * @return Resultado de la subida (URL y fileId)
     */
    public ImageUploadResultDto uploadImage(String figureId, String titulo, MultipartFile file) {

        validate(file);

        byte[] compressedBytes = compressionService.compress(file);
        System.out.println("ImageService.uploadImage: originalContentType=" + file.getContentType() + ", originalSize=" + (file == null ? 0 : file.getSize()) + ", compressedSize=" + (compressedBytes == null ? 0 : compressedBytes.length));

        String fileName = generateFileName(figureId, titulo, file);

        // Si la compresión falla y devuelve 0 bytes, fallback a los bytes originales
        if (compressedBytes == null || compressedBytes.length == 0) {
            try {
                System.out.println("ImageService: compresión devolvió 0 bytes, usando bytes originales como fallback");
                compressedBytes = file.getBytes();
                String ext = getExtensionFromContentType(file.getContentType());
                if (ext != null && !ext.isBlank()) {
                    fileName = fileName.replaceFirst("\\.[^\\.]+$", "." + ext);
                }
                System.out.println("ImageService: fallback fileName=" + fileName + ", size=" + (compressedBytes == null ? 0 : compressedBytes.length));
            } catch (Exception e) {
                System.out.println("ImageService: error al obtener bytes originales para fallback: " + e.getMessage());
            }
        }

        FileUploadResponse response = upload(
                compressedBytes,
                fileName,
                normalizeFolder("manitas-Crochet"));

        return new ImageUploadResultDto(
                response.url().orElseThrow(() -> new ImageUploadException("ImageKit no devolvió una URL válida")),
                response.fileId().orElseThrow(() -> new ImageUploadException("ImageKit no devolvió un fileId válido")));
    }

    /**
     * Elimina una imagen de ImageKit a partir de su fileId.
     *
     * @param fileId identificador único devuelto por ImageKit al subir la imagen
     */
    public void deleteImage(String fileId) {

        if (fileId == null || fileId.isBlank()) {
            throw new InvalidImageException(
                    "El fileId de la imagen es obligatorio");
        }

        try {

            imageKitClient.files().delete(
                    FileDeleteParams.builder()
                            .fileId(fileId)
                            .build());

        } catch (ImageKitException ex) {

            throw new ImageDeleteException(
                    "No se pudo eliminar la imagen de ImageKit",
                    ex);

        } catch (Exception ex) {

            throw new ImageDeleteException(
                    "Error inesperado al eliminar la imagen",
                    ex);
        }
    }

    /**
     * Realiza la subida a ImageKit.
     */
    private FileUploadResponse upload(
            byte[] compressedBytes,
            String fileName,
            String folder) {

        try {
            System.out.println("Uploading image to ImageKit: fileName=" + fileName + ", bytes=" + (compressedBytes == null ? 0 : compressedBytes.length));

            FileUploadParams params = FileUploadParams.builder()
                    .file(new ByteArrayInputStream(compressedBytes))
                    .fileName(fileName)
                    .folder(folder)
                    .useUniqueFileName(true)
                    .build();

            return imageKitClient.files().upload(params);

        } catch (Exception ex) {
            System.out.println("Error en FileUploadParams " + ex.getMessage());
            throw new ImageUploadException(
                    "No se pudo subir la imagen a ImageKit",
                    ex);
        }
    }

    /**
     * Valida que exista un archivo y sea una imagen.
     */
    private void validate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new InvalidImageException(
                    "El archivo está vacío");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new InvalidImageException(
                    "El archivo debe ser una imagen válida");
        }
    }

    /**
     * Genera un nombre único para ImageKit.
     */
    private String generateFileName(
            String figureId,
            String titulo,
            MultipartFile file) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        return figureId
                + "_"
                + sanitizeFileName(titulo)
                + "_"
                + timestamp
                + ".webp";
    }

    /**
     * Elimina caracteres problemáticos.
     */
    private String sanitizeFileName(String fileName) {

        return fileName
                .toLowerCase()
                .replaceAll("[^a-z0-9-_]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Garantiza que la carpeta empiece por "/".
     */
    private String normalizeFolder(String folder) {

        if (folder == null || folder.isBlank()) {
            return "/";
        }

        String normalized = folder.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        return normalized;
    }

    private String getExtensionFromContentType(String contentType) {
        if (contentType == null) return null;
        switch (contentType.toLowerCase()) {
            case "image/png":
                return "png";
            case "image/jpeg":
            case "image/jpg":
                return "jpg";
            case "image/gif":
                return "gif";
            case "image/webp":
                return "webp";
            default:
                return "bin";
        }
    }
}
package com.manitascrochet.backend.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.luciad.imageio.webp.WebPWriteParam;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageProcessingException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.InvalidImageException;

@Service
public class ImageCompressionService {

    private static final int MAX_DIMENSION = 1000;

    private static final float WEBP_QUALITY = 0.75f;

    /**
     * Convierte la imagen original a WebP optimizado.
     */
    public byte[] compress(MultipartFile file) {

        BufferedImage original = readImage(file);

        BufferedImage resized = resizeIfNeeded(original);

        return convertToWebp(resized);
    }

    /**
     * Lee la imagen recibida.
     */
    private BufferedImage readImage(MultipartFile file) {

        try {

            BufferedImage image = ImageIO.read(
                    new ByteArrayInputStream(file.getBytes()));

            if (image == null) {
                throw new InvalidImageException(
                        "El archivo no contiene una imagen válida");
            }

            return image;

        } catch (IOException ex) {

            throw new InvalidImageException(
                    "No se pudo leer la imagen enviada",
                    ex);
        }
    }

    /**
     * Redimensiona únicamente si supera el tamaño máximo.
     */
    private BufferedImage resizeIfNeeded(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        if (width <= MAX_DIMENSION &&
                height <= MAX_DIMENSION) {

            return image;
        }

        double scale =
                (double) MAX_DIMENSION /
                        Math.max(width, height);

        int newWidth = (int) Math.round(width * scale);
        int newHeight = (int) Math.round(height * scale);

        BufferedImage resized =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(
                image,
                0,
                0,
                newWidth,
                newHeight,
                null);

        graphics.dispose();

        return resized;
    }

    /**
     * Convierte la imagen a WebP.
     */
    private byte[] convertToWebp(BufferedImage image) {

        Iterator<ImageWriter> writers =
                ImageIO.getImageWritersByMIMEType("image/webp");

        if (!writers.hasNext()) {

            throw new ImageProcessingException(
                    "No se encontró soporte WebP. Verifica la dependencia webp-imageio.");
        }

        ImageWriter writer = writers.next();

                try (
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ImageOutputStream ios = ImageIO.createImageOutputStream(baos)
                ) {

                        // Log info útil para depuración
                        try {
                                System.out.println("convertToWebp: using ImageWriter=" + writer.getClass().getName());
                                String[] types = writer.getOriginatingProvider().getMIMETypes();
                                System.out.println("convertToWebp: provider mime types=" + String.join(",", types));
                        } catch (Exception ignore) {
                        }

                        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
                        writeParam.setCompressionMode(WebPWriteParam.MODE_EXPLICIT);

                        String[] compressionTypes = writeParam.getCompressionTypes();
                        if (compressionTypes != null && compressionTypes.length > 0) {
                                int idx = Math.min(WebPWriteParam.LOSSY_COMPRESSION, compressionTypes.length - 1);
                                writeParam.setCompressionType(compressionTypes[idx]);
                        }

                        writeParam.setCompressionQuality(WEBP_QUALITY);

                        // Forzar tipo compatible (ARGB) para evitar incompatibilidades con el writer
                        BufferedImage converted = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = converted.createGraphics();
                        g.drawImage(image, 0, 0, null);
                        g.dispose();

                        writer.setOutput(ios);

                        writer.write(null, new IIOImage(converted, null, null), writeParam);

                        ios.flush();
                        baos.flush();

                        byte[] result = baos.toByteArray();

                        if (result == null || result.length == 0) {
                                throw new ImageProcessingException("La conversión WebP devolvió 0 bytes");
                        }

                        return result;

                } catch (IOException ex) {

                        throw new ImageProcessingException("Error al convertir la imagen a WebP", ex);

                } finally {

                        writer.dispose();
                }
    }
}
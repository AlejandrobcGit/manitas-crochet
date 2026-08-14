package com.manitascrochet.backend.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.service.ImageService;
import com.manitascrochet.backend.util.ByteArrayMultipartFile;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "app.test-upload.enabled", havingValue = "true")
@Order(1)
@RequiredArgsConstructor
public class TestUploadRunner implements CommandLineRunner {

    private final ImageService imageService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[TestUploadRunner] Iniciando prueba de subida a ImageKit...");

        String figureId = "6a5c8e3e3af2080f592b0896";
        String titulo = "Pikachu";

        try {
            byte[] principal = generateSampleImageBytes(400, 400, Color.YELLOW);
            ByteArrayMultipartFile principalFile = new ByteArrayMultipartFile(principal, "imagenPrincipal", "pikachu.png", "image/png");

            ImageUploadResultDto resp = imageService.uploadImage(figureId, titulo, principalFile);
            System.out.println("[TestUploadRunner] Imagen principal subida: url=" + resp.getUrl() + " fileId=" + resp.getFileId());

            int count = 1;
            for (Color c : List.of(Color.ORANGE, Color.YELLOW, Color.ORANGE, Color.YELLOW, Color.ORANGE)) {
                byte[] b = generateSampleImageBytes(200, 200, c);
                ByteArrayMultipartFile f = new ByteArrayMultipartFile(b, "imagenesSecundarias", "pikachu-" + count + ".png", "image/png");
                ImageUploadResultDto r = imageService.uploadImage(figureId, titulo + "-" + count, f);
                System.out.println("[TestUploadRunner] Secundaria " + count + ": url=" + r.getUrl() + " fileId=" + r.getFileId());
                count++;
            }

        } catch (Exception ex) {
            System.out.println("[TestUploadRunner] Error en prueba: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private byte[] generateSampleImageBytes(int w, int h, Color color) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        }
    }
}

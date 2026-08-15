package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.luciad.imageio.webp.WebPWriteParam;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageProcessingException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.InvalidImageException;

/**
 * NOTA DE IMPLEMENTACIÓN:
 * ImageCompressionService no tiene dependencias inyectables (no hay nada que
 * mockear con @Mock/@InjectMocks). Internamente usa clases concretas de
 * terceros instanciadas directamente (ImageIO estático, WebPWriteParam,
 * ByteArrayOutputStream), y el escritor WebP real depende de una librería
 * nativa (webp-imageio) que puede no estar disponible en todos los entornos
 * de CI/SO. Para que estos tests sean deterministas y no dependan de esa
 * librería nativa, se usan las capacidades de Mockito 5 (ya activas en el
 * proyecto, según los logs de build que muestran el inline-mock-maker
 * autoadjuntándose):
 * - mockStatic(ImageIO.class) para controlar read/getImageWritersByMIMEType/
 * createImageOutputStream.
 * - mockConstruction(WebPWriteParam.class) para controlar getCompressionTypes().
 * - mockConstruction(ByteArrayOutputStream.class) para forzar el caso
 * result == null, que el contrato real de toByteArray() nunca produce.
 * Las imágenes en sí son BufferedImage reales en memoria (sin códecs
 * nativos), así que la lógica de redimensionado se ejercita de verdad.
 */
class ImageCompressionServiceTest {

    private final ImageCompressionService service = new ImageCompressionService();

    private MultipartFile archivoCualquiera() {
        return new MockMultipartFile("file", "foto.png", "image/png", new byte[] { 1, 2, 3 });
    }

    /**
     * Deja el pipeline de ImageIO listo para un compress() exitoso:
     * read() devuelve la imagen indicada, hay un writer WebP disponible y
     * createImageOutputStream() escribe bytesEscritos (si no es null) en el
     * ByteArrayOutputStream real que usa el propio servicio, simulando que
     * el writer produjo esos bytes.
     */
    private ImageWriter prepararPipelineExitoso(
            MockedStatic<ImageIO> imageIO,
            BufferedImage imagenDecodificada,
            byte[] bytesEscritos) throws IOException {

        ImageWriter writer = mock(ImageWriter.class);
        ImageOutputStream ios = mock(ImageOutputStream.class);

        imageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(imagenDecodificada);
        imageIO.when(() -> ImageIO.getImageWritersByMIMEType("image/webp"))
                .thenReturn(List.of(writer).iterator());
        imageIO.when(() -> ImageIO.createImageOutputStream(any()))
                .thenAnswer(invocation -> {
                    OutputStream out = invocation.getArgument(0);
                    if (bytesEscritos != null) {
                        out.write(bytesEscritos);
                    }
                    return ios;
                });

        return writer;
    }

    // ---------------------------------------------------------------
    // camino feliz
    // ---------------------------------------------------------------

    @Test
    void compressCaminoFeliz_devuelveBytesWebp() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }))) {

            BufferedImage imagenPequena = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagenPequena, new byte[] { 9, 9, 9, 9 });

            byte[] resultado = service.compress(archivoCualquiera());

            assertThat(resultado).isNotEmpty();
        }
    }

    // ---------------------------------------------------------------
    // readImage
    // ---------------------------------------------------------------

    @Test
    void compressArchivoNoEsImagenValida_lanzaInvalidImageException() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            imageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(null);

            assertThatThrownBy(() -> service.compress(archivoCualquiera()))
                    .isInstanceOf(InvalidImageException.class);
        }
    }

    @Test
    void compressErrorAlLeerBytesDelArchivo_lanzaInvalidImageException() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("no se pudo leer"));

        assertThatThrownBy(() -> service.compress(file))
                .isInstanceOf(InvalidImageException.class);
    }

    // ---------------------------------------------------------------
    // resizeIfNeeded
    // ---------------------------------------------------------------

    @Test
    void compressImagenAnchaPorEncimaDelMaximo_seRedimensionaSinError() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }))) {

            BufferedImage imagenAncha = new BufferedImage(2000, 100, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagenAncha, new byte[] { 1, 2, 3 });

            byte[] resultado = service.compress(archivoCualquiera());

            assertThat(resultado).isNotEmpty();
        }
    }

    @Test
    void compressImagenAltaPorEncimaDelMaximo_seRedimensionaSinError() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }))) {

            BufferedImage imagenAlta = new BufferedImage(100, 2000, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagenAlta, new byte[] { 1, 2, 3 });

            byte[] resultado = service.compress(archivoCualquiera());

            assertThat(resultado).isNotEmpty();
        }
    }

    // ---------------------------------------------------------------
    // convertToWebp
    // ---------------------------------------------------------------

    @Test
    void compressSinWriterWebpDisponible_lanzaImageProcessingException() {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class)) {
            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            imageIO.when(() -> ImageIO.read(any(InputStream.class))).thenReturn(imagen);
            imageIO.when(() -> ImageIO.getImageWritersByMIMEType("image/webp"))
                    .thenReturn(Collections.emptyIterator());

            assertThatThrownBy(() -> service.compress(archivoCualquiera()))
                    .isInstanceOf(ImageProcessingException.class);
        }
    }

    @Test
    void compressCompressionTypesNulo_noFallaYComprimeIgualmente() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes()).thenReturn(null))) {

            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagen, new byte[] { 5, 5 });

            byte[] resultado = service.compress(archivoCualquiera());

            assertThat(resultado).isNotEmpty();
        }
    }

    @Test
    void compressCompressionTypesVacio_noFallaYComprimeIgualmente() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[0]))) {

            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagen, new byte[] { 5, 5 });

            byte[] resultado = service.compress(archivoCualquiera());

            assertThat(resultado).isNotEmpty();
        }
    }

    @Test
    void compressResultadoVacio_lanzaImageProcessingException() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }))) {

            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            // bytesEscritos = null -> el writer "no produce" ningún byte real.
            prepararPipelineExitoso(imageIO, imagen, null);

            assertThatThrownBy(() -> service.compress(archivoCualquiera()))
                    .isInstanceOf(ImageProcessingException.class);
        }
    }

    @Test
    void compressResultadoNulo_lanzaImageProcessingException() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }));
                MockedConstruction<ByteArrayOutputStream> baosMock = mockConstruction(ByteArrayOutputStream.class,
                        (mockBaos, context) -> when(mockBaos.toByteArray()).thenReturn(null))) {

            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            prepararPipelineExitoso(imageIO, imagen, new byte[] { 1 });

            assertThatThrownBy(() -> service.compress(archivoCualquiera()))
                    .isInstanceOf(ImageProcessingException.class);
        }
    }

    @Test
    void compressErrorDeEscrituraDelWriter_lanzaImageProcessingExceptionYLiberaElWriter() throws Exception {
        try (MockedStatic<ImageIO> imageIO = mockStatic(ImageIO.class);
                MockedConstruction<WebPWriteParam> webpParam = mockConstruction(WebPWriteParam.class,
                        (mockParam, context) -> when(mockParam.getCompressionTypes())
                                .thenReturn(new String[] { "lossy" }))) {

            BufferedImage imagen = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ImageWriter writer = prepararPipelineExitoso(imageIO, imagen, new byte[] { 1 });
            doThrow(new IOException("fallo de escritura"))
                    .when(writer).write(isNull(), any(IIOImage.class), any(ImageWriteParam.class));

            assertThatThrownBy(() -> service.compress(archivoCualquiera()))
                    .isInstanceOf(ImageProcessingException.class);

            verify(writer).dispose();
        }
    }
}
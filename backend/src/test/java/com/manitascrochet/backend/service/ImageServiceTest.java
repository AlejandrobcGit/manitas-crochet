package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.manitascrochet.backend.dto.ImageUploadResultDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageDeleteException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ImageUploadException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.InvalidImageException;

import io.imagekit.client.ImageKitClient;
import io.imagekit.errors.ImageKitException;
import io.imagekit.models.files.FileDeleteParams;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;

/**
 * NOTA DE IMPLEMENTACIÓN:
 * ImageKitClient es el SDK externo de ImageKit. No conozco el tipo concreto
 * que devuelve imageKitClient.files(), así que el mock se crea con
 * RETURNS_DEEP_STUBS para poder encadenar
 * imageKitClient.files().upload(...)/.delete(...) sin nombrar ese tipo
 * intermedio explícitamente (Java lo infiere del propio SDK, que ya compila
 * en el código de producción). Por la misma razón, estos tests no asumen
 * getters de FileUploadParams/FileDeleteParams (objetos del SDK): solo
 * verifican el contrato observable de ImageService (valores devueltos,
 * excepciones lanzadas e interacciones con sus colaboradores).
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    ImageCompressionService compressionService;

    ImageKitClient imageKitClient;

    ImageService service;

    @BeforeEach
    void setUp() {
        imageKitClient = mock(ImageKitClient.class, RETURNS_DEEP_STUBS);
        service = new ImageService(imageKitClient, compressionService);
    }

    private MultipartFile imagenValida() {
        return new MockMultipartFile(
                "file",
                "oso.png",
                "image/png",
                new byte[] { 1, 2, 3, 4 });
    }

    private FileUploadResponse respuestaExitosa(String url, String fileId) {
        FileUploadResponse response = mock(FileUploadResponse.class);
        lenient().when(response.url()).thenReturn(Optional.ofNullable(url));
        lenient().when(response.fileId()).thenReturn(Optional.ofNullable(fileId));
        return response;
    }

    // ---------------------------------------------------------------
    // uploadImage
    // ---------------------------------------------------------------

    @Test
    void uploadImageCaminoFeliz_devuelveUrlYFileId() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/oso.webp", "file-abc");
        when(compressionService.compress(file)).thenReturn(new byte[] { 9, 9, 9 });
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        ImageUploadResultDto result = service.uploadImage("f1", "Oso Amigurumi", file);

        assertThat(result.getUrl()).isEqualTo("https://cdn.imagekit.io/oso.webp");
        assertThat(result.getFileId()).isEqualTo("file-abc");
        verify(compressionService, times(1)).compress(file);
        verify(imageKitClient.files(), times(1)).upload(any(FileUploadParams.class));
    }

    @Test
    void uploadImageConTituloConCaracteresEspeciales_noLanzaExcepcion() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/x.webp", "file-x");
        when(compressionService.compress(file)).thenReturn(new byte[] { 1 });
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        ImageUploadResultDto result = service.uploadImage("f1", "  Amigurumi Búho! (#1) ", file);

        assertThat(result).isNotNull();
    }

    @Test
    void uploadImageArchivoNulo_lanzaInvalidImageException() {
        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", null))
                .isInstanceOf(InvalidImageException.class);

        verify(compressionService, never()).compress(any());
    }

    @Test
    void uploadImageArchivoVacio_lanzaInvalidImageException() {
        MultipartFile vacio = new MockMultipartFile("file", "vacio.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", vacio))
                .isInstanceOf(InvalidImageException.class);

        verify(compressionService, never()).compress(any());
    }

    @Test
    void uploadImageContentTypeNoEsImagen_lanzaInvalidImageException() {
        MultipartFile pdf = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[] { 1 });

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", pdf))
                .isInstanceOf(InvalidImageException.class);

        verify(compressionService, never()).compress(any());
    }

    @Test
    void uploadImageContentTypeNulo_lanzaInvalidImageException() {
        MultipartFile sinTipo = new MockMultipartFile("file", "raro", null, new byte[] { 1 });

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", sinTipo))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void uploadImageCompresionDevuelveArrayVacio_usaBytesOriginalesComoFallback() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/fallback.webp", "file-fb");
        when(compressionService.compress(file)).thenReturn(new byte[0]);
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        ImageUploadResultDto result = service.uploadImage("f1", "titulo", file);

        assertThat(result.getUrl()).isEqualTo("https://cdn.imagekit.io/fallback.webp");
        verify(imageKitClient.files(), times(1)).upload(any(FileUploadParams.class));
    }

    @Test
    void uploadImageCompresionDevuelveNull_usaBytesOriginalesComoFallback() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/fallback2.webp", "file-fb2");
        when(compressionService.compress(file)).thenReturn(null);
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        ImageUploadResultDto result = service.uploadImage("f1", "titulo", file);

        assertThat(result.getUrl()).isEqualTo("https://cdn.imagekit.io/fallback2.webp");
    }

    @Test
    void uploadImageImageKitLanzaExcepcion_lanzaImageUploadException() {
        MultipartFile file = imagenValida();
        when(compressionService.compress(file)).thenReturn(new byte[] { 1 });
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenThrow(new RuntimeException("ImageKit no disponible"));

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", file))
                .isInstanceOf(ImageUploadException.class);
    }

    @Test
    void uploadImageRespuestaSinUrl_lanzaImageUploadException() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa(null, "file-abc");
        when(compressionService.compress(file)).thenReturn(new byte[] { 1 });
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", file))
                .isInstanceOf(ImageUploadException.class);
    }

    @Test
    void uploadImageRespuestaSinFileId_lanzaImageUploadException() {
        MultipartFile file = imagenValida();
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/x.webp", null);
        when(compressionService.compress(file)).thenReturn(new byte[] { 1 });
        when(imageKitClient.files().upload(any(FileUploadParams.class)))
                .thenReturn(response);

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", file))
                .isInstanceOf(ImageUploadException.class);
    }

    // ---------------------------------------------------------------
    // deleteImage
    // ---------------------------------------------------------------

    @Test
    void deleteImageCaminoFeliz_delegaEnImageKit() {
        service.deleteImage("file-123");

        verify(imageKitClient.files(), times(1)).delete(any(FileDeleteParams.class));
    }

    @Test
    void deleteImageFileIdNulo_lanzaInvalidImageException() {
        assertThatThrownBy(() -> service.deleteImage(null))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void deleteImageFileIdEnBlanco_lanzaInvalidImageException() {
        assertThatThrownBy(() -> service.deleteImage("   "))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void deleteImageImageKitLanzaImageKitException_lanzaImageDeleteException() throws Exception {
        ImageKitException fallo = mock(ImageKitException.class);
        var files = imageKitClient.files();
        doThrow(fallo).when(files).delete(any(FileDeleteParams.class));

        assertThatThrownBy(() -> service.deleteImage("file-123"))
                .isInstanceOf(ImageDeleteException.class);
    }

    @Test
    void deleteImageErrorInesperado_lanzaImageDeleteException() {
        var files = imageKitClient.files();
        doThrow(new RuntimeException("fallo inesperado"))
                .when(files).delete(any(FileDeleteParams.class));

        assertThatThrownBy(() -> service.deleteImage("file-123"))
                .isInstanceOf(ImageDeleteException.class);
    }

    // ---------------------------------------------------------------
    // uploadImage - ramas de cobertura adicionales (fallback y switch)
    // ---------------------------------------------------------------

    @Test
    void uploadImageFallbackFalloAlObtenerBytesOriginales_lanzaImageUploadException() throws Exception {
        // MultipartFile mockeado (no MockMultipartFile) para poder forzar
        // que getBytes() falle durante el fallback de compresión.
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenThrow(new IOException("no se pudo leer el archivo"));
        when(compressionService.compress(file)).thenReturn(null);

        assertThatThrownBy(() -> service.uploadImage("f1", "titulo", file))
                .isInstanceOf(ImageUploadException.class);

        verify(imageKitClient.files(), never()).upload(any(FileUploadParams.class));
    }

    @ParameterizedTest
    @ValueSource(strings = { "image/jpeg", "image/gif", "image/webp", "image/bmp" })
    void uploadImageFallbackConDistintosContentTypes_noLanzaExcepcion(String contentType) {
        MultipartFile file = new MockMultipartFile("file", "img", contentType, new byte[] { 1, 2, 3 });
        FileUploadResponse response = respuestaExitosa("https://cdn.imagekit.io/x.webp", "file-x");
        when(compressionService.compress(file)).thenReturn(new byte[0]);
        when(imageKitClient.files().upload(any(FileUploadParams.class))).thenReturn(response);

        ImageUploadResultDto result = service.uploadImage("f1", "titulo", file);

        assertThat(result).isNotNull();
    }

    // ---------------------------------------------------------------
    // Métodos privados de soporte (normalizeFolder, getExtensionFromContentType)
    //
    // NOTA: en el flujo público estos métodos siempre reciben argumentos
    // válidos (la carpeta es un literal fijo, y el content-type ya fue
    // validado antes de llegar aquí), así que sus ramas defensivas
    // (folder nulo/blanco, contentType nulo) nunca se ejecutan a través
    // de la API pública. Se invocan aquí por reflexión únicamente para
    // ejercitar esas ramas de forma aislada, sin modificar la visibilidad
    // ni el comportamiento de ImageService.
    // ---------------------------------------------------------------
    @Nested
    class MetodosPrivadosDeSoporte {

        @Test
        void normalizeFolderConNull_devuelveBarraRaiz() throws Exception {
            assertThat(invocarNormalizeFolder(null)).isEqualTo("/");
        }

        @Test
        void normalizeFolderConBlanco_devuelveBarraRaiz() throws Exception {
            assertThat(invocarNormalizeFolder("   ")).isEqualTo("/");
        }

        @Test
        void normalizeFolderConBarraYaPresente_noLaDuplica() throws Exception {
            assertThat(invocarNormalizeFolder("/ya-tiene-barra")).isEqualTo("/ya-tiene-barra");
        }

        @Test
        void getExtensionFromContentTypeConNull_devuelveNull() throws Exception {
            assertThat(invocarGetExtensionFromContentType(null)).isNull();
        }

        private String invocarNormalizeFolder(String folder) throws Exception {
            Method metodo = ImageService.class.getDeclaredMethod("normalizeFolder", String.class);
            metodo.setAccessible(true);
            return (String) metodo.invoke(service, folder);
        }

        private String invocarGetExtensionFromContentType(String contentType) throws Exception {
            Method metodo = ImageService.class.getDeclaredMethod("getExtensionFromContentType", String.class);
            metodo.setAccessible(true);
            return (String) metodo.invoke(service, contentType);
        }
    }
}
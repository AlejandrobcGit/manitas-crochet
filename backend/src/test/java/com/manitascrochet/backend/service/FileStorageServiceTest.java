package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;

class FileStorageServiceTest {
    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService();
    }

    @AfterEach
    void clean() {
        service.delete("test-id_titulo.png");
    }

    @Test
    void guardaCargaYEliminaImagen() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.PNG", "image/png", new byte[] { 1, 2, 3 });
        String name = service.store(file, "test-id", "título! ");
        assertThat(name).isEqualTo("test-id_t_tulo__.png");
        Resource resource = service.loadAsResource(name);
        assertThat(resource.exists()).isTrue();
        service.delete(name);
        assertThat(resource.exists()).isFalse();
    }

    @Test
    void rechazaEntradasInseguras() {
        MockMultipartFile empty = new MockMultipartFile("f", "x.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> service.store(empty, "i", "t")).hasMessage("Fichero vacío");
        MockMultipartFile text = new MockMultipartFile("f", "x.txt", "text/plain", new byte[] { 1 });
        assertThatThrownBy(() -> service.store(text, "i", "t")).hasMessage("Solo se permiten imágenes");
        assertThatThrownBy(
                () -> service.store(new MockMultipartFile("f", "x.exe", "image/png", new byte[] { 1 }), "i", "t"))
                .hasMessage("Formato de imagen no permitido");
        assertThatThrownBy(() -> service.loadAsResource("../secret")).isInstanceOf(RuntimeException.class);
    }

    @Test
    void generaUrlYDevuelveVacioSinRequest() {
        assertThat(service.getFileUrl(null)).isEmpty();
        assertThat(service.getFileUrl("x.png")).isEmpty();
        HttpServletRequest request = mock(HttpServletRequest.class);
        ReflectionTestUtils.setField(service, "request", request);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/img/x.png"));
        assertThat(service.getFileUrl("x.png")).contains("/img/x.png");
    }
}

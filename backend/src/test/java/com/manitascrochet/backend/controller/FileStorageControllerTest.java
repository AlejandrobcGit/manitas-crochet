package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.manitascrochet.backend.model.Figura;
import com.manitascrochet.backend.service.FileStorageService;
import com.manitascrochet.backend.service.FiguraService;

@ExtendWith(MockitoExtension.class)
class FileStorageControllerTest {
    @Mock FileStorageService files;
    @Mock FiguraService figuras;
    @InjectMocks FileStorageController controller;

    private Figura figura(String image) { Figura f = new Figura(); f.setNombre("Oso"); f.setImagenPrincipal(image); return f; }

    @Test void subeImagenYActualizaFigura() {
        Figura figura = figura(null);
        MockMultipartFile file = new MockMultipartFile("file", "oso.png", "image/png", new byte[] { 1 });
        when(figuras.obtenerPorId("f1")).thenReturn(figura);
        when(files.store(file, "f1", "Oso")).thenReturn("f1_oso.png");
        assertThat(controller.uploadImg("f1", file).getBody()).contains("f1_oso.png");
        assertThat(figura.getImagenPrincipal()).isEqualTo("f1_oso.png");
    }

    @Test void cargaImagenDecodificadaYConfiguraCache() {
        ByteArrayResource resource = new ByteArrayResource(new byte[] { 1 });
        when(files.loadAsResource("mi foto.jpg")).thenReturn(resource);
        var response = controller.getImage("mi%20foto.jpg");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(resource);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/jpeg");
        verify(files).loadAsResource("mi foto.jpg");
    }

    @Test void devuelve404ParaNombreVacio() {
        assertThat(controller.getImage("").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(files);
    }

    @Test void obtieneUrlYEliminaImagen() {
        Figura figura = figura("oso.png");
        when(figuras.obtenerPorId("f1")).thenReturn(figura);
        when(files.getFileUrl("oso.png")).thenReturn("http://localhost/oso.png");
        assertThat(controller.getImgUrl("f1").getBody()).isEqualTo("http://localhost/oso.png");
        assertThat(controller.deleteImg("f1").getBody()).contains("eliminada");
        assertThat(figura.getImagenPrincipal()).isNull();
        verify(files).delete("oso.png");
    }

    @Test void rechazaFiguraSinImagen() {
        when(figuras.obtenerPorId("f1")).thenReturn(figura(" "));
        assertThatThrownBy(() -> controller.getImgUrl("f1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("La figura no tiene imagen asociada");
        assertThatThrownBy(() -> controller.deleteImg("f1"))
                .isInstanceOf(ResponseStatusException.class);
    }
}

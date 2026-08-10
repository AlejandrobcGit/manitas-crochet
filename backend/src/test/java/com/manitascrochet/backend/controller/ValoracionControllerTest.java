package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.service.ValoracionService;

@ExtendWith(MockitoExtension.class)
class ValoracionControllerTest {
    @Mock ValoracionService service;
    @InjectMocks ValoracionController controller;

    @Test void delegaPuntuacionYUsuario() {
        UserDetailsImpl user = new UserDetailsImpl();
        Valoracion valoracion = new Valoracion();
        when(service.valorarFigura("f1", 5, user)).thenReturn(valoracion);
        assertThat(controller.valorarFigura("f1", new ValoracionDto(5), user)).isSameAs(valoracion);
        verify(service).valorarFigura("f1", 5, user);
    }
}

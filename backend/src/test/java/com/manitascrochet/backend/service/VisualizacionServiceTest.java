package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.manitascrochet.backend.model.Visualizacion;
import com.manitascrochet.backend.repository.VisualizacionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class VisualizacionServiceTest {
    @Mock
    VisualizacionRepository repository;
    @InjectMocks
    VisualizacionService service;

    @Test
    void ignoraAdmin() {
        UserDetailsImpl admin = new UserDetailsImpl();
        admin.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        service.marcarVisualizacion("f", admin);
        verifyNoInteractions(repository);
    }

    @Test
    void guardaInvitadoSinLogin() {
        service.marcarVisualizacion("f", null);
        ArgumentCaptor<Visualizacion> cap = ArgumentCaptor.forClass(Visualizacion.class);
        verify(repository).save(cap.capture());
        assertThat(cap.getValue().getUsuarioId()).isNull();
        assertThat(cap.getValue().getFiguraId()).isEqualTo("f");
        assertThat(cap.getValue().getFecha()).isNotNull();
    }
}

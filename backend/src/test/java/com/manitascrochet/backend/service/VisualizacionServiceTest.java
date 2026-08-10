package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
    void ignoraInvitadoYAdmin() {
        service.marcarVisualizacion("f", null);
        verifyNoInteractions(repository);
        UserDetailsImpl admin = new UserDetailsImpl();
        admin.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        service.marcarVisualizacion("f", admin);
        verifyNoInteractions(repository);
    }

    @Test
    void guardaUsuarioNormal() {
        UserDetailsImpl user = new UserDetailsImpl();
        user.setId("u");
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        service.marcarVisualizacion("f", user);
        ArgumentCaptor<Visualizacion> cap = ArgumentCaptor.forClass(Visualizacion.class);
        verify(repository).save(cap.capture());
        assertThat(cap.getValue().getUsuarioId()).isEqualTo("u");
        assertThat(cap.getValue().getFiguraId()).isEqualTo("f");
        assertThat("fecha de visualización", cap.getValue().getFecha() != null, equalTo(true));
    }
}

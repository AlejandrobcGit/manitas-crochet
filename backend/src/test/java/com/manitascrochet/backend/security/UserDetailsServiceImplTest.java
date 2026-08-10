package com.manitascrochet.backend.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.EmailNotFoundException;
import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
    @Mock
    UsuarioRepository repository;
    @InjectMocks
    UserDetailsServiceImpl service;

    private Usuario user() {
        return new Usuario("id", "ana", "ana@test", "hash", Rol.USER, true);
    }

    @Test
    void cargaPorUsernameYEmail() {
        when(repository.findByUsername("ana")).thenReturn(Optional.of(user()));
        assertThat(service.loadUserByUsername("ana").getUsername()).isEqualTo("ana");
        when(repository.findByEmail("ana@test")).thenReturn(Optional.of(user()));
        assertThat(service.loadUserByEmail("ana@test").getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void fallaSiNoExiste() {
        when(repository.findByUsername("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("x")).isInstanceOf(UsernameNotFoundException.class);
        when(repository.findByEmail("x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByEmail("x")).isInstanceOf(EmailNotFoundException.class);
    }
}

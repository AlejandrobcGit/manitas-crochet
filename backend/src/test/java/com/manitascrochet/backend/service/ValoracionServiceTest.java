package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.manitascrochet.backend.dto.ValoracionDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.ValoracionInvalidaException;
import com.manitascrochet.backend.model.Valoracion;
import com.manitascrochet.backend.repository.ValoracionRepository;
import com.manitascrochet.backend.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class ValoracionServiceTest {

    @Mock
    private ValoracionRepository repository;

    @InjectMocks
    private ValoracionService service;

    private UserDetailsImpl user() {
        UserDetailsImpl u = new UserDetailsImpl();
        u.setId("u1");
        return u;
    }

    @Test
    void creaYActualizaValoracion() {

        when(repository.findByUsuarioIdAndFiguraId("u1", "f1"))
                .thenReturn(Optional.empty());

        Valoracion created =
                service.valorarFigura("f1", 5, user());

        assertThat(created.getUsuarioId()).isEqualTo("u1");
        assertThat(created.getPuntuacion()).isEqualTo(5);

        verify(repository).save(created);

        when(repository.findByUsuarioIdAndFiguraId("u1", "f1"))
                .thenReturn(Optional.of(created));

        Valoracion updated =
                service.valorarFigura("f1", 3, user());

        assertThat(updated.getPuntuacion()).isEqualTo(3);
    }

    @Test
    void validaUsuarioYPuntuacion() {

        assertThat(service.valorarFigura("f", 2, null))
                .isNull();

        assertThatThrownBy(
                () -> service.valorarFigura("f", 6, user()))
                .isInstanceOf(ValoracionInvalidaException.class);

        assertThatThrownBy(
                () -> service.valorarFigura("f", null, user()))
                .isInstanceOf(ValoracionInvalidaException.class);

        // Rama puntuación < 0
        assertThatThrownBy(
                () -> service.valorarFigura("f", -1, user()))
                .isInstanceOf(ValoracionInvalidaException.class);
    }

    @Test
    void resumeConsultaYElimina() {

        Valoracion a = new Valoracion();
        a.setPuntuacion(4);

        Valoracion b = new Valoracion();
        b.setPuntuacion(2);

        when(repository.findByFiguraId("f"))
                .thenReturn(List.of(a, b));

        assertThat(
                service.obtenerResumenValoraciones("f")
                        .getValoracionMedia())
                .isEqualTo(3.0);

        assertThat(
                service.obtenerValoracionUsuario(null, "f")
                        .getPuntuacion())
                .isZero();

        service.eliminarValoracionesPorFigura("f");

        verify(repository).deleteByFiguraId("f");
    }

    @Test
    void obtenerValoracionUsuarioConFiguraNula() {

        ValoracionDto dto =
                service.obtenerValoracionUsuario("u1", null);

        assertThat(dto.getPuntuacion()).isZero();
    }

    @Test
    void obtenerValoracionUsuarioConAmbosParametrosNulos() {

        ValoracionDto dto =
                service.obtenerValoracionUsuario(null, null);

        assertThat(dto.getPuntuacion()).isZero();
    }

    @Test
    void obtenerValoracionUsuarioExistente() {

        Valoracion valoracion = new Valoracion();
        valoracion.setPuntuacion(4);

        when(repository.findByUsuarioIdAndFiguraId("u1", "f1"))
                .thenReturn(Optional.of(valoracion));

        ValoracionDto dto =
                service.obtenerValoracionUsuario("u1", "f1");

        assertThat(dto.getPuntuacion()).isEqualTo(4);
    }

    @Test
    void obtenerValoracionUsuarioNoExistente() {

        when(repository.findByUsuarioIdAndFiguraId("u1", "f1"))
                .thenReturn(Optional.empty());

        ValoracionDto dto =
                service.obtenerValoracionUsuario("u1", "f1");

        assertThat(dto.getPuntuacion()).isZero();
    }
}
package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import com.manitascrochet.backend.model.Dificultad;

class DificultadControllerTest {
    @Test void devuelveTodasLasDificultades() {
        assertThat(new DificultadController().obtenerTodas()).containsExactly(Dificultad.values());
    }
}

package com.manitascrochet.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.manitascrochet.backend.service.EmailService;

@ExtendWith(MockitoExtension.class)
class MailControllerTest {
    @Mock EmailService emailService;
    @InjectMocks MailController controller;

    @Test void enviaCorreoDePrueba() throws MessagingException {
        assertThat(controller.test().getBody()).isEqualTo("Correo enviado");
        verify(emailService).enviar("alejo@blancocuesta.com", "Prueba Manitas Crochet",
                "El envío de correo funciona correctamente");
    }
}

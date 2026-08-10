package com.manitascrochet.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    JavaMailSender sender;
    @Mock
    MimeMessage message;
    @InjectMocks
    EmailService service;

    @Test
    void construyeYEnviaCorreoHtml() throws Exception {
        when(sender.createMimeMessage()).thenReturn(message);
        ReflectionTestUtils.setField(service, "remitente", "no-reply@test.local");
        service.enviar("dest@test.local", "Asunto", "<b>contenido</b>");
        verify(sender).send(message);
        verify(sender).createMimeMessage();
        assertThat(message).isNotNull();
    }
}

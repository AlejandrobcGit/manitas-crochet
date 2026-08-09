package com.manitascrochet.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.service.EmailService;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class MailController {

    private final EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<String> test() throws MessagingException {

        emailService.enviar(
                "alejo@blancocuesta.com",
                "Prueba Manitas Crochet",
                "El envío de correo funciona correctamente");

        return ResponseEntity.ok("Correo enviado");
    }
}
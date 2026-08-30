package com.manitascrochet.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.manitascrochet.backend.exception.GlobalExceptionHandler.EmailNotFoundException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.EmailYaVerificadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenExpiradoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenInvalidoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenInvalidoRecuperacionException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenYaUsadoException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.UsernameNotFoundException;
import com.manitascrochet.backend.model.TokenVerificacion;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.TokenVerificacionRepository;
import com.manitascrochet.backend.repository.UsuarioRepository;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificacionEmailService {

        private final TokenVerificacionRepository tokenVerificacionRepository;
        private final EmailService emailService;
        private final UsuarioRepository usuarioRepository;
        private final MongoTemplate mongoTemplate; // necesario para el findAndModify atómico
        private final PasswordEncoder passwordEncoder;

        @Value("${APP_FRONTEND_URL}") // Obtiene el dirección del servidor desde aplicaciones.properties
        private String app_frontend_url;

        @Value("${APP_PROTOCOLO}") // Obtiene el protocolo del servidor desde aplicaciones.properties
        private String protocolo;

        public void enviarCorreoVerificacion(String email) throws MessagingException {

                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(email));

                if (usuario.isEmailVerificado()) {
                        throw new EmailYaVerificadoException();
                }

                String token = UUID.randomUUID().toString();

                TokenVerificacion tokenVerificacion = TokenVerificacion.builder()
                                .usuarioId(usuario.getId())
                                .token(token)
                                .fechaExpiracion(LocalDateTime.now().plusMinutes(15))
                                .usado(false)
                                .build();

                tokenVerificacionRepository.save(tokenVerificacion);

                String enlace = protocolo + "://" + app_frontend_url + "/verificar-email?token=" + token;

                String mensajeHtml = String.format(
                                """
                                                <!DOCTYPE html>
                                                <html>
                                                <body style="font-family: Arial, sans-serif; color: #333;">
                                                    <h2>Bienvenido a Manitas Crochet</h2>
                                                    <p>Para activar tu cuenta pulsa el siguiente enlace:</p>
                                                    <p>
                                                        <a href="%s" style="background-color: #d63384; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                                                            Activar cuenta
                                                        </a>
                                                    </p>
                                                    <p>O copia y pega este enlace en tu navegador:</p>
                                                    <p>%s</p>
                                                </body>
                                                </html>
                                                """,
                                enlace, enlace);

                emailService.enviar(
                                usuario.getEmail(),
                                "Manitas Crochet - Verificación de cuenta",
                                mensajeHtml);
        }

        public void verificarCuenta(String token) {

                // 1) Intento atómico: solo marca usado=true si sigue sin usar Y no expiró.
                // Esto es lo que elimina la condición de carrera entre dos requests
                // concurrentes con el mismo token.
                Query query = new Query(Criteria.where("token").is(token)
                                .and("usado").is(false)
                                .and("fechaExpiracion").gt(LocalDateTime.now()));

                Update update = new Update().set("usado", true);

                TokenVerificacion tokenVerificacion = mongoTemplate.findAndModify(
                                query,
                                update,
                                FindAndModifyOptions.options().returnNew(true),
                                TokenVerificacion.class);

                // 2) Si no se pudo actualizar, el token no cumplía la condición.
                // Hacemos una lectura aparte SOLO para dar un mensaje de error preciso
                // (esta lectura ya no tiene riesgo de carrera: no escribe nada).
                if (tokenVerificacion == null) {
                        TokenVerificacion existente = tokenVerificacionRepository.findByToken(token)
                                        .orElseThrow(() -> new TokenInvalidoException());

                        if (existente.isUsado()) {
                                throw new TokenYaUsadoException();
                        }

                        if (existente.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                                throw new TokenExpiradoException();
                        }

                        // Si llega aquí, es un estado inesperado (p. ej. condición de carrera
                        // resuelta a favor de otra request justo entre el findAndModify y esta
                        // lectura). Lo tratamos como ya usado por seguridad.
                        throw new TokenYaUsadoException();
                }

                // 3) Ya tenemos garantía de que SOLO esta llamada pudo marcar el token,
                // así que ahora sí actualizamos el usuario de forma segura.
                Usuario usuario = usuarioRepository.findById(tokenVerificacion.getUsuarioId())
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                tokenVerificacion.getUsuarioId()));

                if (usuario.isEmailVerificado()) {
                        throw new EmailYaVerificadoException();
                }

                usuario.setEmailVerificado(true);
                usuarioRepository.save(usuario);
        }

        // ---------------------------------------------------------
        // Recuperar contraseña
        // ---------------------------------------------------------

        public void enviarCorreoRecuperacion(String email) throws MessagingException {
                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseThrow(() -> new EmailNotFoundException(email));

                String token = UUID.randomUUID().toString();

                TokenVerificacion tokenVerificacion = TokenVerificacion.builder()
                                .usuarioId(usuario.getId())
                                .token(token)
                                .fechaExpiracion(LocalDateTime.now().plusMinutes(5))
                                .usado(false)
                                .build();

                tokenVerificacionRepository.save(tokenVerificacion);

                String enlace = protocolo + "://" + app_frontend_url + "/recuperar-contrasena?token=" + token;

                String mensajeHtml = String.format(
                                """
                                                <!DOCTYPE html>
                                                <html>
                                                <body style="font-family: Arial, sans-serif; color: #333;">
                                                    <h2>Bienvenido a Manitas Crochet</h2>
                                                    <p>Para recuperar tu contraseña, haz clic en el siguiente enlace:</p>
                                                    <p>
                                                        <a href="%s" style="background-color: #d63384; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                                                            Recuperar contraseña
                                                        </a>
                                                    </p>
                                                    <p>O copia y pega este enlace en tu navegador:</p>
                                                    <p>%s</p>
                                                    <p style="font-size: 0.85em; color: #777;">Este enlace expira en 5 minutos. Si no solicitaste este cambio, puedes ignorar este correo.</p>
                                                </body>
                                                </html>
                                                """,
                                enlace, enlace);

                emailService.enviar(
                                usuario.getEmail(),
                                "Manitas Crochet - Recuperación de contraseña",
                                mensajeHtml);
        }

        public void restablecerContrasena(String token, String nuevaContrasena) {
                TokenVerificacion tokenVerificacion = tokenVerificacionRepository.findByToken(token)
                                .orElseThrow(TokenInvalidoException::new);

                if (tokenVerificacion.isUsado()
                                || tokenVerificacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                        throw new TokenInvalidoRecuperacionException();
                }

                Usuario usuario = usuarioRepository.findById(tokenVerificacion.getUsuarioId())
                                .orElseThrow(TokenInvalidoRecuperacionException::new);

                usuario.setPassword(passwordEncoder.encode(nuevaContrasena));
                usuarioRepository.save(usuario);

                tokenVerificacion.setUsado(true);
                tokenVerificacionRepository.save(tokenVerificacion);
        }

}
package com.manitascrochet.backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.dto.security.JwtResponseDto;
import com.manitascrochet.backend.dto.security.LoginDto;
import com.manitascrochet.backend.dto.security.MessageResponse;
import com.manitascrochet.backend.dto.security.ResetPasswordRequest;
import com.manitascrochet.backend.dto.security.SignupDto;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.EmailNotFoundException;
import com.manitascrochet.backend.exception.GlobalExceptionHandler.TokenInvalidoException;
import com.manitascrochet.backend.exception.security.EmailAlreadyExistsException;
import com.manitascrochet.backend.exception.security.InvalidRefreshTokenException;
import com.manitascrochet.backend.exception.security.UsernameAlreadyExistsException;
import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.UsuarioRepository;
import com.manitascrochet.backend.security.JwtUtils;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.security.UserDetailsServiceImpl;
import com.manitascrochet.backend.service.VerificacionEmailService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final VerificacionEmailService verificacionEmailService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.http-only:true}")
    private boolean cookieHttpOnly;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsServiceImpl,
            VerificacionEmailService verificacionEmailService) {

        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.verificacionEmailService = verificacionEmailService;
    }

    // ---------------------------------------------------------
    // LOGIN / SIGNIN
    // ---------------------------------------------------------

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginDto loginDto,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String rol = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ERROR");

        return ResponseEntity.ok(new JwtResponseDto(
                accessToken,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                rol,
                userDetails.isEmailVerificado()));
    }

    // ---------------------------------------------------------
    // REFRESH TOKEN
    // ---------------------------------------------------------

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            throw new InvalidRefreshTokenException("No se encontró el refresh token en la cookie.");
        }

        String email;
        try {
            email = jwtUtils.getEmailFromJwtToken(refreshToken);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Refresh token inválido.");
        }

        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token expirado o inválido.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(email);

        String newAccessToken = jwtUtils.generateTokenFromEmail(email);
        String newRefreshToken = jwtUtils.generateRefreshTokenFromEmail(email);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        String rol = userDetails.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ERROR");

        return ResponseEntity.ok(new JwtResponseDto(
                newAccessToken,
                "Bearer",
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                rol,
                userDetails.isEmailVerificado()));
    }

    // ---------------------------------------------------------
    // SIGNUP / REGISTRO
    // ---------------------------------------------------------

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody SignupDto signUpRequest) {

        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Ya existe un usuario con ese nombre.");
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con ese email.");
        }

        Usuario user = new Usuario(
                null,
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                Rol.USER,
                false);

        usuarioRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado correctamente"));
    }

    // ---------------------------------------------------------
    // Creación de Admnistrador - por usuario de rol Administrador
    // ---------------------------------------------------------

    @PostMapping("/admin/crear-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearAdmin(@Valid @RequestBody SignupDto signUpRequest) {

        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameAlreadyExistsException("Ya existe un usuario con ese nombre.");
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailAlreadyExistsException("Ya existe un usuario con ese email.");
        }

        Usuario admin = new Usuario(
                null,
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                Rol.ADMIN,
                false);

        usuarioRepository.save(admin);

        return ResponseEntity.ok(new MessageResponse("Administrador creado correctamente"));
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", deleteCookie.toString());

        return ResponseEntity.ok(new MessageResponse("Logout exitoso"));
    }

    // ---------------------------------------------------------
    // Recuperar contraseña
    // ---------------------------------------------------------

    @PostMapping("/enviarCorreoRecuperar-contrasena")
    public ResponseEntity<?> enviarCorreoRecuperarContrasena(
            @RequestParam @Email(message = "El formato del correo no es válido") @NotBlank(message = "El correo es obligatorio") String email) {

        try {
            verificacionEmailService.enviarCorreoRecuperacion(email);
            log.info("Correo se envió correctamnete " + email);
        } catch (EmailNotFoundException ex) {
            // No revelamos que el correo no existe: solo lo registramos internamente
            log.info("Solicitud de recuperación para correo no registrado: {}", email);
        } catch (MessagingException ex) {
            // Falló el envío del correo (SMTP caído, etc). No se expone al cliente.
            log.error("Error al enviar correo de recuperación a {}: {}", email, ex.getMessage(), ex);
        } catch (Exception ex) {
            // Cualquier otro error inesperado (ej. fallo al guardar el token en BD)
            log.error("Error inesperado en recuperación de contraseña para {}: {}", email, ex.getMessage(), ex);
        }

        // Respuesta siempre idéntica, exista o no la cuenta,
        // para evitar enumeración de usuarios.
        return ResponseEntity.ok(
                new MessageResponse(
                        "Si existe una cuenta asociada a ese correo, se ha enviado un email con instrucciones de recuperación"));
    }

    @PostMapping("/restablecer-contrasena")
    public ResponseEntity<?> restablecerContrasena(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            verificacionEmailService.restablecerContrasena(request.getToken(), request.getNuevaContrasena());
            return ResponseEntity.ok(new MessageResponse("Contraseña actualizada correctamente"));
        } catch (TokenInvalidoException ex) {
            log.info("Intento de restablecer contraseña con token inválido/expirado");
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("El enlace no es válido o ha expirado. Solicita uno nuevo."));
        }
    }

    // ---------------------------------------------------------
    // Verificación correo
    // ---------------------------------------------------------
    @GetMapping("/enviarcorreoverificar")
    public ResponseEntity<Map<String, String>> EnviarCorreoverificar(
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws MessagingException {

        verificacionEmailService.enviarCorreoVerificacion(userDetails.getEmail());

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, String>> verificar(
            @RequestParam String token) throws MessagingException {

        verificacionEmailService.verificarCuenta(token);

        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
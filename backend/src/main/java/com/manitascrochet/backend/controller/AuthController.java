package com.manitascrochet.backend.controller;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.manitascrochet.backend.dto.security.JwtResponseDto;
import com.manitascrochet.backend.dto.security.LoginDto;
import com.manitascrochet.backend.dto.security.MessageResponse;
import com.manitascrochet.backend.dto.security.SignupDto;
import com.manitascrochet.backend.exception.security.EmailAlreadyExistsException;
import com.manitascrochet.backend.exception.security.InvalidRefreshTokenException;
import com.manitascrochet.backend.exception.security.UsernameAlreadyExistsException;
import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.UsuarioRepository;
import com.manitascrochet.backend.security.JwtUtils;
import com.manitascrochet.backend.security.UserDetailsImpl;
import com.manitascrochet.backend.security.UserDetailsServiceImpl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public AuthController(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            PasswordEncoder encoder,
            JwtUtils jwtUtils,
            UserDetailsServiceImpl userDetailsServiceImpl) {

        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    // ---------------------------------------------------------
    // LOGIN / SIGNIN
    // ---------------------------------------------------------

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @RequestBody LoginDto loginDto,
            HttpServletResponse response) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
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
                rol));
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

        String username;
        try {
            username = jwtUtils.getUserNameFromJwtToken(refreshToken);
        } catch (Exception e) {
            throw new InvalidRefreshTokenException("Refresh token inválido.");
        }

        if (!jwtUtils.validateJwtToken(refreshToken)) {
            throw new InvalidRefreshTokenException("Refresh token expirado o inválido.");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsServiceImpl.loadUserByUsername(username);

        String newAccessToken = jwtUtils.generateTokenFromUsername(username);
        String newRefreshToken = jwtUtils.generateRefreshTokenFromUsername(username);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
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
                rol));
    }

    // ---------------------------------------------------------
    // SIGNUP / REGISTRO
    // ---------------------------------------------------------

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(
            @RequestBody SignupDto signUpRequest) {

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
                Rol.USER);

        usuarioRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado correctamente"));
    }

    // ---------------------------------------------------------
    // Creación de Admnistrador - por usuario de rol Administrador
    // ---------------------------------------------------------

    @PostMapping("/admin/crear-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearAdmin(@RequestBody SignupDto signUpRequest) {

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
                Rol.ADMIN);

        usuarioRepository.save(admin);

        return ResponseEntity.ok(new MessageResponse("Administrador creado correctamente"));
    }

    // ---------------------------------------------------------
    // LOGOUT
    // ---------------------------------------------------------

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok(new MessageResponse("Logout exitoso"));
    }
}
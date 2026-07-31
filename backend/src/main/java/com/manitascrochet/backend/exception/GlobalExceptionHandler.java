package com.manitascrochet.backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.manitascrochet.backend.exception.security.EmailAlreadyExistsException;
import com.manitascrochet.backend.exception.security.InvalidRefreshTokenException;
import com.manitascrochet.backend.exception.security.UsernameAlreadyExistsException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── Model : Figuras ───────────────────────────────────────────────────────

    // 409 - Categoría duplicada
    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<ApiError> handleCategoriaDuplicada(CategoriaDuplicadaException ex) {
        return build(HttpStatus.CONFLICT, "CATEGORIA_EXISTENTE", ex.getMessage());
    }

    // 409 - Color duplicado
    @ExceptionHandler(ColorDuplicadoException.class)
    public ResponseEntity<ApiError> handleColorDuplicado(ColorDuplicadoException ex) {
        return build(HttpStatus.CONFLICT, "COLOR_EXISTENTE", ex.getMessage());
    }

    @ExceptionHandler(CodigoColorDuplicadoException.class)
    public ResponseEntity<ApiError> handleCodigoColorDuplicado(CodigoColorDuplicadoException ex) {
        return build(HttpStatus.CONFLICT, "CODIGO_COLOR_EXISTENTE", ex.getMessage());
    }

    // 404 - Recursos no encontrados
    @ExceptionHandler({
            CategoriaNoEncontradaException.class,
            ColorNoEncontradoException.class,
            FiguraNoEncontradaException.class
    })
    public ResponseEntity<ApiError> manejarNoEncontrado(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ValoracionInvalidaException.class)
    public ResponseEntity<ApiError> handleValoracionInvalida(ValoracionInvalidaException ex) {
        return build(HttpStatus.BAD_REQUEST,"VALORACION_INVALIDA",ex.getMessage());
    }

    // ─── Seguridad ─────────────────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Credenciales incorrectas.");
    }

    // Reemplaza AuthorizationDeniedException → compatible con todas las versiones
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "No tienes permisos para realizar esta acción.");
    }

    // ─── Autenticación / Registro ──────────────────────────────────────────────

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameExists(UsernameAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage());
    }

    // ─── Validación de entrada (@Valid) ────────────────────────────────────────

    // JSON normal ej: /signin, /signup
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError error = ApiError.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("VALIDATION_ERROR")
                .mensaje("Hay errores de validación en los datos enviados.")
                .timestamp(LocalDateTime.now().toString())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // error de Formularios multipart ej: /api/book/update
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(cv -> {
            String field = cv.getPropertyPath().toString();
            field = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
            fieldErrors.put(field, cv.getMessage());
        });

        ApiError error = ApiError.builder()
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("VALIDATION_ERROR")
                .mensaje("Hay errores de validación en los datos enviados.")
                .timestamp(LocalDateTime.now().toString())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // JSON mal formado (sintaxis inválida, comas sobrantes, tipos incompatibles,
    // etc.)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleJsonInvalido(HttpMessageNotReadableException ex) {
        log.warn("JSON inválido en el request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "JSON_INVALIDO",
                "El cuerpo de la petición no es válido. Verifica los campos enviados.");
    }

    // ─── Errores de request ────────────────────────────────────────────────────

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        String msg = "Parámetro requerido ausente: " + ex.getParameterName();
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", msg);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String msg = "El parámetro '" + ex.getName() + "' tiene un tipo incorrecto.";
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER_TYPE", msg);
    }

    // ─── Base de datos ─────────────────────────────────────────────────────────

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleFK(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "INTEGRITY_VIOLATION",
                "No se puede eliminar el recurso porque está referenciado por otra entidad.");
    }

    // ─── Fallback ──────────────────────────────────────────────────────────────

    // No exponemos ex.getMessage() al cliente: puede filtrar detalles internos
    // (rutas, nombres de clase, estructura de BD). El detalle real queda en el log.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Error interno no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo.");
    }

    // ─── Builder interno ───────────────────────────────────────────────────────

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String mensaje) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .error(code)
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now().toString())
                .fieldErrors(null)
                .build();
        return ResponseEntity.status(status).body(error);
    }

    // ==========================
    // Excepciones personalizadas
    // ==========================

    public static class CategoriaDuplicadaException extends RuntimeException {

        public CategoriaDuplicadaException(String nombre) {
            super("La categoría '" + nombre + "' ya existe");
        }
    }

    public static class CategoriaNoEncontradaException extends RuntimeException {

        public CategoriaNoEncontradaException(String id) {
            super("La categoría con id '" + id + "' no existe");
        }
    }

    public static class ColorDuplicadoException extends RuntimeException {

        public ColorDuplicadoException(String valor) {
            super("El color '" + valor + "' ya existe");
        }
    }

    public static class CodigoColorDuplicadoException extends RuntimeException {

        public CodigoColorDuplicadoException(String valor) {
            super("El código de color '" + valor + "' ya existe");
        }
    }

    public static class ColorNoEncontradoException extends RuntimeException {

        public ColorNoEncontradoException(String id) {
            super("El color con id '" + id + "' no existe");
        }
    }

    public static class FiguraNoEncontradaException extends RuntimeException {

        public FiguraNoEncontradaException(String id) {
            super("La figura con id '" + id + "' no existe");
        }
    }

    public static class ValoracionInvalidaException extends RuntimeException {

        public ValoracionInvalidaException() {
            super("La puntuación debe estar entre 1 y 5");
        }
    }

}
package com.manitascrochet.backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.manitascrochet.backend.exception.security.EmailAlreadyExistsException;
import com.manitascrochet.backend.exception.security.InvalidRefreshTokenException;
import com.manitascrochet.backend.exception.security.UsernameAlreadyExistsException;

import jakarta.mail.MessagingException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // private static final Logger log =
    // LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler({
            CategoriaEnUsoException.class,
            ColorEnUsoException.class })
    public ResponseEntity<ApiError> handleRecusoUtilizado(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, "VALOR_EN_USO", ex.getMessage());
    }

    // 404 - Recursos no encontrados
    @ExceptionHandler({
            CategoriaNoEncontradaException.class,
            ColorNoEncontradoException.class,
            FiguraNoEncontradaException.class,
            ComentarioNoEncontradoException.class })
    public ResponseEntity<ApiError> manejarNoEncontrado(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(ValoracionInvalidaException.class)
    public ResponseEntity<ApiError> handleValoracionInvalida(ValoracionInvalidaException ex) {
        return build(HttpStatus.BAD_REQUEST, "VALORACION_INVALIDA", ex.getMessage());
    }

    // ─── ImageKit ─────────────────────────────────────────────────────────────

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiError> handleInvalidImage(InvalidImageException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_IMAGE", ex.getMessage());
    }

    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ApiError> handleImageProcessing(ImageProcessingException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "IMAGE_PROCESSING_ERROR", ex.getMessage());
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiError> handleImageUpload(ImageUploadException ex) {
        log.error("Error al subir imagen: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_GATEWAY, "IMAGE_UPLOAD_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "El archivo supera el tamaño máximo permitido");
    }

    @ExceptionHandler(ImageDeleteException.class)
    public ResponseEntity<ApiError> handleImageDelete(ImageDeleteException ex) {
        return build(HttpStatus.BAD_GATEWAY, "IMAGE_DELETE_FAILED", "No se pudo eliminar la imagen");
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

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ApiError> handleEmailNotFound(EmailNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ApiError> handleTokenInvalido(TokenInvalidoException ex) {
        return build(HttpStatus.BAD_REQUEST, "TOKEN_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(TokenYaUsadoException.class)
    public ResponseEntity<ApiError> handleTokenYaUsado(TokenYaUsadoException ex) {
        return build(HttpStatus.BAD_REQUEST, "TOKEN_YA_USADO", ex.getMessage());
    }

    @ExceptionHandler({ TokenExpiradoException.class,
            TokenInvalidoRecuperacionException.class })
    public ResponseEntity<ApiError> handleTokenExpirado(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRADO", ex.getMessage());
    }

    // ─── Autenticación / Registro ──────────────────────────────────────────────

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsuarioNotFound(UsernameNotFoundException ex) {
        return build(HttpStatus.UNAUTHORIZED, "SESION_INVALIDA", "Credenciales inválidas o sesión expirada.");
    }

    @ExceptionHandler(DisableSinUpException.class)
    public ResponseEntity<ApiError> handleDisableSinup(DisableSinUpException ex) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "REGISTRO_DESHABILITADO", ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameExists(UsernameAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(PrivacyPolicyNotAcceptedException.class)
    public ResponseEntity<ApiError> handlePrivacyPolicyNotAccepted(PrivacyPolicyNotAcceptedException ex) {
        return build(HttpStatus.BAD_REQUEST, "PRIVACY_POLICY_NOT_ACCEPTED", ex.getMessage());
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

    // error de Formularios multipart
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

    // ─── Errores para el correo electronico
    // ─────────────────────────────────────────────────
    @ExceptionHandler(MailAuthenticationException.class)
    public ResponseEntity<ApiError> handleMailAuthenticationException(MailAuthenticationException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_AUTH_ERROR",
                "Error de configuración del servicio de correo ->" + ex.getMessage());
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiError> handleMailException(MailException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_ERROR",
                "No se pudo enviar el correo electrónico ->" + ex.getMessage());
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ApiError> handleMessagingException(MessagingException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_ERROR",
                "Error al enviar el correo electrónico ->" + ex.getMessage());
    }

    @ExceptionHandler(EmailYaVerificadoException.class)
    public ResponseEntity<ApiError> handleEmailYaVerificado(EmailYaVerificadoException ex) {
        return build(HttpStatus.CONFLICT, "EMAIL_YA_VERIFICADO", ex.getMessage());
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

    public static class ComentarioNoEncontradoException extends RuntimeException {

        public ComentarioNoEncontradoException() {
            super("No se puede eliminar/actualizar el comentario de otros usuarios");
        }
    }

    public static class EmailNotFoundException extends RuntimeException {

        public EmailNotFoundException(String email) {
            super("El usuario con email '" + email + "' no existe");
        }
    }

    public static class TokenInvalidoException extends RuntimeException {

        public TokenInvalidoException() {
            super("El token de verificación es inválido");
        }
    }

    public static class TokenYaUsadoException extends RuntimeException {

        public TokenYaUsadoException() {
            super("El token de verificación ya ha sido usado");
        }
    }

    public static class TokenExpiradoException extends RuntimeException {

        public TokenExpiradoException() {
            super("El token de verificación ha expirado");
        }
    }

    public static class TokenInvalidoRecuperacionException extends RuntimeException {
        public TokenInvalidoRecuperacionException() {
            super("El token de recuperación no es válido o ha expirado");
        }
    }

    public static class UsernameNotFoundException extends RuntimeException {

        public UsernameNotFoundException(String userId) {
            super("El usuario con id '" + userId + "' no existe");
        }
    }

    public static class EmailYaVerificadoException extends RuntimeException {

        public EmailYaVerificadoException() {
            super("Este correo ya fue verificado anteriormente");
        }
    }

    public static class ImageProcessingException extends RuntimeException {

        public ImageProcessingException(String message) {
            super(message);
        }

        public ImageProcessingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ImageUploadException extends RuntimeException {

        public ImageUploadException(String message) {
            super(message);
        }

        public ImageUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidImageException extends RuntimeException {

        public InvalidImageException(String message) {
            super(message);
        }

        public InvalidImageException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ImageDeleteException extends RuntimeException {
        public ImageDeleteException(String message) {
            super(message);
        }

        public ImageDeleteException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class CategoriaEnUsoException extends RuntimeException {
        public CategoriaEnUsoException() {
            super("No se puede eliminar categorias asignada");
        }
    }

    public static class ColorEnUsoException extends RuntimeException {
        public ColorEnUsoException() {
            super("No se puede eliminar colores asignados");
        }
    }

    public static class DisableSinUpException extends RuntimeException {
        public DisableSinUpException() {
            super("La funcionalidad de cuentas de usuario se encuentra temporalmente deshabilitada.");
        }
    }

    public static class PrivacyPolicyNotAcceptedException extends RuntimeException {
        public PrivacyPolicyNotAcceptedException() {
            super("Debes aceptar la Política de Privacidad para registrarte.");
        }
    }
}
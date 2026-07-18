package com.manitascrochet.backend.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Respuesta estándar de error
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String mensaje) {
    }

    // 409 - Categoría duplicada
    @ExceptionHandler(CategoriaDuplicadaException.class)
    public ResponseEntity<ErrorResponse> manejarCategoriaDuplicada(
            CategoriaDuplicadaException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 409 - Color duplicado
    @ExceptionHandler(ColorDuplicadoException.class)
    public ResponseEntity<ErrorResponse> manejarColorDuplicado(
            ColorDuplicadoException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(CodigoColorDuplicadoException.class)
    public ResponseEntity<ErrorResponse> manejarCodigoColorDuplicado(
            CodigoColorDuplicadoException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 404 - Recursos no encontrados
    @ExceptionHandler({
            CategoriaNoEncontradaException.class,
            ColorNoEncontradoException.class,
            FiguraNoEncontradaException.class
    })
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(
            RuntimeException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 500 - Error general no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Error interno del servidor");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarEnumInvalido(
            HttpMessageNotReadableException ex) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Valor de dificultad no válido. Valores permitidos: PRINCIPIANTE, INTERMEDIO, AVANZADO");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
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
}
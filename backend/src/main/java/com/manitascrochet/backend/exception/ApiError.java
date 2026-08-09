package com.manitascrochet.backend.exception;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiError {

    int status;        // 404, 401, 422...
    String error;      // "BOOK_NOT_FOUND", "BAD_CREDENTIALS"...
    String mensaje;    // Mensaje legible para el usuario
    String timestamp;  // Cuándo ocurrió el error
    Map<String, String> fieldErrors; // Solo en errores de validación @Valid
}
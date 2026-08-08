package com.manitascrochet.backend.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseDto {

  private String accessToken;
  private String tokenType; // "Bearer"
  private String id;
  private String nombre;
  private String email;
  private String rol;
  private boolean emailVerificado;

  // 🔥 Constructor corto para /refresh
  public JwtResponseDto(String accessToken, String tokenType) {
    this.accessToken = accessToken;
    this.tokenType = tokenType;
  }
}

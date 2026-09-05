package com.manitascrochet.backend.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupDto {

  @NotBlank(message = "El nombre de usuario es obligatorio")
  private String username;

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email no tiene un formato válido")
  private String email;

 /* ; Mejora de seguridad un endpoint publico no debe permite selecionar el rol
  private String rol */

  @NotBlank(message = "La contraseña es obligatoria")
  @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
  private String password;

  private Boolean politicaPrivacidadAceptada;

}
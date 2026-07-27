package com.manitascrochet.backend.dto.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class SignupDto {

  private String username;

  private String email;

 /* ; Mejora de seguridad un endpoint publico no debe permite selecionar el rol
  private String rol */

  private String password;

}
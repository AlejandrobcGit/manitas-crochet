package com.manitascrochet.backend.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

  @Id
  private String id;

  // findByUsername + existsByUsername (login, registro, validador de username)
  @Indexed(name = "idx_usr_username")
  private String username;

  // findByEmail + existsByEmail (login con email, registro, verificación)
  @Indexed(name = "idx_usr_email")
  private String email;

  private String password;

  private Rol rol;

  private boolean emailVerificado;

  private boolean politicaPrivacidadAceptada;

  private LocalDateTime fechaAceptacionPrivacidad;

  public Usuario(String id, String username, String email, String password, Rol rol, boolean emailVerificado) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.password = password;
    this.rol = rol;
    this.emailVerificado = emailVerificado;
  }
}
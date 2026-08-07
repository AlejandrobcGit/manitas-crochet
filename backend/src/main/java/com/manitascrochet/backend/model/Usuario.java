package com.manitascrochet.backend.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "usuarios")
public class Usuario {

  @Id
  private String id;

  private String username;

  private String email;

  private String password;

  private Rol rol;

  private boolean emailVerificado;
}
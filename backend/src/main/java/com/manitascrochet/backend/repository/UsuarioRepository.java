package com.manitascrochet.backend.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.manitascrochet.backend.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
  Optional<Usuario> findByEmail(String email);
  Boolean existsByUsername(String username);
  Boolean existsByEmail(String email);
  Optional<Usuario> findByUsername(String username);

}

package com.manitascrochet.backend;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.manitascrochet.backend.model.Rol;
import com.manitascrochet.backend.model.Usuario;
import com.manitascrochet.backend.repository.UsuarioRepository;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
		return args -> {
			if (usuarioRepository.count() == 0) {
				Usuario admin = new Usuario(
						null,
						"admin",
						"admin@manitascrochet.com",
						encoder.encode("12345"),
						Rol.ADMIN,
						false,
						true,
						LocalDateTime.now());
				usuarioRepository.save(admin);
				System.out.println("✅ Usuario admin creado por defecto (admin / 12345)");
				Usuario user = new Usuario(
						null,
						"user",
						"user@manitascrochet.com",
						encoder.encode("12345"),
						Rol.USER,
						false,
						true,
						LocalDateTime.now());
				usuarioRepository.save(user);
				System.out.println("✅ Usuario user creado por defecto (user / 12345)");
			}
		};
	}
}

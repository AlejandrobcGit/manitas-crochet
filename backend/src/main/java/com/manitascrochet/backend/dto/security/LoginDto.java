package com.manitascrochet.backend.dto.security;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginDto {
	@NotBlank (message = "Indicar usuario")
	private String username;

	@NotBlank (message = "Indicar contraseña")
	private String password;
}

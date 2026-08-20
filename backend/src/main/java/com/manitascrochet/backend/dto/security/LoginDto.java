package com.manitascrochet.backend.dto.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginDto {
	
	@NotBlank(message = "El email es obligatorio")
	@Email(message = "El email no tiene un formato válido")
	private String email;

	@NotBlank(message = "Indicar contraseña")
	private String password;
}

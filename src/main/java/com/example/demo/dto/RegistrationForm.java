package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {

	@NotBlank(message = "Le nom est obligatoire")
	private String name;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "Email invalide")
	private String email;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
	private String password;

	@NotBlank(message = "La confirmation est obligatoire")
	private String confirmPassword;

	public boolean isPasswordConfirmed() {
		return password != null && password.equals(confirmPassword);
	}
}

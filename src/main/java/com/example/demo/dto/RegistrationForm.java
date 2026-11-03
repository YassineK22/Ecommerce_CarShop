package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationForm(
		@NotBlank(message = "Le nom est obligatoire")
		String name,
		@NotBlank(message = "L'email est obligatoire")
		@Email(message = "Email invalide")
		String email,
		@NotBlank(message = "Le mot de passe est obligatoire")
		@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
		String password,
		@NotBlank(message = "La confirmation est obligatoire")
		String confirmPassword) {

	public boolean isPasswordConfirmed() {
		return password != null && password.equals(confirmPassword);
	}
}

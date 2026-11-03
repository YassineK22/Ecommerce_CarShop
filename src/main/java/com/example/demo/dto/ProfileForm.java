package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileForm(
		@NotBlank(message = "Le nom est obligatoire")
		String name,
		@NotBlank(message = "L'email est obligatoire")
		@Email(message = "Email invalide")
		String email,
		String currentPassword,
		String newPassword,
		String confirmNewPassword) {

	public boolean wantsPasswordChange() {
		return newPassword != null && !newPassword.isBlank();
	}

	public boolean isNewPasswordConfirmed() {
		return newPassword != null && newPassword.equals(confirmNewPassword);
	}
}

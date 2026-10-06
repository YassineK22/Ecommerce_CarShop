package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {

	@NotBlank(message = "Le nom est obligatoire")
	private String name;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "Email invalide")
	private String email;

	private String currentPassword;

	private String newPassword;

	private String confirmNewPassword;

	public boolean wantsPasswordChange() {
		return newPassword != null && !newPassword.isBlank();
	}

	public boolean isNewPasswordConfirmed() {
		return newPassword != null && newPassword.equals(confirmNewPassword);
	}
}

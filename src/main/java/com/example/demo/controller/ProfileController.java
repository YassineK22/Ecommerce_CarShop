package com.example.demo.controller;

import com.example.demo.dto.ProfileForm;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

	@Autowired
	private UserService userService;

	@GetMapping("/profile")
	public String profilePage(@AuthenticationPrincipal User user, Model model) {
		ProfileForm form = new ProfileForm(user.getName(), user.getEmail(), null, null, null);
		model.addAttribute("profileForm", form);
		return "profile";
	}

	@PostMapping("/profile")
	public String updateProfile(
			@AuthenticationPrincipal User user,
			@Valid @ModelAttribute("profileForm") ProfileForm form,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (form.wantsPasswordChange()) {
			if (form.newPassword().length() < 8) {
				bindingResult.rejectValue("newPassword", "size", "Le nouveau mot de passe doit contenir au moins 8 caractères");
			}
			if (form.currentPassword() == null || form.currentPassword().isBlank()) {
				bindingResult.rejectValue("currentPassword", "required", "Indiquez votre mot de passe actuel");
			} else if (!userService.matchesPassword(user, form.currentPassword())) {
				bindingResult.rejectValue("currentPassword", "invalid", "Mot de passe actuel incorrect");
			}
			if (!form.isNewPasswordConfirmed()) {
				bindingResult.rejectValue("confirmNewPassword", "match", "Les mots de passe ne correspondent pas");
			}
		}

		if (!bindingResult.hasErrors() && userService.isEmailTakenByOtherUser(form.email(), user.getId())) {
			bindingResult.rejectValue("email", "unique", "Cet email est déjà utilisé");
		}

		if (bindingResult.hasErrors()) {
			return "profile";
		}

		userService.updateProfile(user, form);
		redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour.");
		return "redirect:/profile";
	}
}

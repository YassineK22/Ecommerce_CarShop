package com.example.demo.controller;

import com.example.demo.dto.RegistrationForm;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/register")
	public String registerForm(Model model) {
		model.addAttribute("registrationForm", new RegistrationForm(null, null, null, null));
		return "register";
	}

	@PostMapping("/register")
	public String registerSubmit(
			@Valid @ModelAttribute("registrationForm") RegistrationForm form,
			BindingResult bindingResult) {

		if (!form.isPasswordConfirmed()) {
			bindingResult.rejectValue("confirmPassword", "match", "Les mots de passe ne correspondent pas");
		}

		if (!bindingResult.hasErrors() && userService.isEmailTaken(form.email())) {
			bindingResult.rejectValue("email", "unique", "Cet email est déjà utilisé");
		}

		if (bindingResult.hasErrors()) {
			return "register";
		}

		try {
			userService.register(form);
		} catch (DataIntegrityViolationException ex) {
			// Deux inscriptions simultanées sur le même email : la contrainte unique tranche
			bindingResult.rejectValue("email", "unique", "Cet email est déjà utilisé");
			return "register";
		}
		return "redirect:/login?registered";
	}
}

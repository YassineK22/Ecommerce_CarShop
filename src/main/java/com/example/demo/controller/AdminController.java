package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AnnonceService;
import com.example.demo.service.OffreService;
import com.example.demo.service.UserService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * M5 - Administration.
 * L'accès à /admin/** est déjà réservé au rôle ADMIN (SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AnnonceService annonceService;
	@Autowired
	private UserService userService;
	@Autowired
	private OffreService offreService;

	/**
	 * Tableau de bord : quelques compteurs simples.
	 */
	@GetMapping
	public String dashboard(Model model) {
		model.addAttribute("nbUsers", userService.count());
		model.addAttribute("nbAnnonces", annonceService.count());
		model.addAttribute("nbEncheresOuvertes", annonceService.countEncheresOuvertes());
		model.addAttribute("nbOffres", offreService.count());
		model.addAttribute("nbSignalees", annonceService.countSignalees());
		return "admin/dashboard";
	}

	/**
	 * Modération des annonces : les signalées en premier, avec le bouton de suppression.
	 */
	@GetMapping("/annonces")
	public String annonces(Model model) {
		model.addAttribute("annonces", annonceService.findAllForAdmin());
		return "admin/annonces";
	}

	@PostMapping("/annonces/{id}/delete")
	public String supprimerAnnonce(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			annonceService.deleteByAdmin(id);
			redirectAttributes.addFlashAttribute("successMessage", "Annonce supprimée.");
		} catch (IllegalArgumentException | IOException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/admin/annonces";
	}

	/**
	 * Gestion des comptes : activation / désactivation.
	 */
	@GetMapping("/utilisateurs")
	public String utilisateurs(Model model) {
		model.addAttribute("users", userService.findAll());
		return "admin/utilisateurs";
	}

	@PostMapping("/utilisateurs/{id}/activer")
	public String activerUtilisateur(@PathVariable Long id, @AuthenticationPrincipal User admin,
			RedirectAttributes redirectAttributes) {
		return changerActivation(id, true, admin, redirectAttributes);
	}

	@PostMapping("/utilisateurs/{id}/desactiver")
	public String desactiverUtilisateur(@PathVariable Long id, @AuthenticationPrincipal User admin,
			RedirectAttributes redirectAttributes) {
		return changerActivation(id, false, admin, redirectAttributes);
	}

	private String changerActivation(Long id, boolean active, User admin, RedirectAttributes redirectAttributes) {
		try {
			userService.setActive(id, active, admin);
			redirectAttributes.addFlashAttribute("successMessage",
					active ? "Compte activé." : "Compte désactivé : il ne peut plus se connecter.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/admin/utilisateurs";
	}
}

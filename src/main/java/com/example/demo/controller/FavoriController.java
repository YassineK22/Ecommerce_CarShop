package com.example.demo.controller;

import com.example.demo.entity.Favori;
import com.example.demo.entity.User;
import com.example.demo.service.FavoriService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FavoriController {

	@Autowired
	private FavoriService favoriService;

	/**
	 * Ajoute / retire une annonce des favoris (un seul bouton qui bascule).
	 */
	@PostMapping("/annonces/{id}/favori")
	public String basculer(
			@PathVariable Long id,
			@AuthenticationPrincipal User user,
			@RequestParam(required = false) String retour,
			RedirectAttributes redirectAttributes) {

		try {
			boolean ajoute = favoriService.basculer(id, user);
			redirectAttributes.addFlashAttribute("successMessage",
					ajoute ? "Annonce ajoutée à vos favoris." : "Annonce retirée de vos favoris.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}

		if ("favoris".equals(retour)) {
			return "redirect:/mes-favoris";
		}
		return "redirect:/annonces/" + id;
	}

	@GetMapping("/mes-favoris")
	public String mesFavoris(@AuthenticationPrincipal User user, Model model) {
		List<Favori> favoris = favoriService.getFavoris(user);
		model.addAttribute("favoris", favoris);
		return "favoris";
	}
}

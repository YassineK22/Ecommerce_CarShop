package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.OffreService;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OffreController {

	@Autowired
	private OffreService offreService;

	/**
	 * Place une offre depuis le formulaire de la page détail,
	 * puis redirige vers cette page pour afficher l'historique actualisé.
	 */
	@PostMapping("/annonces/{id}/offres")
	public String placerOffre(
			@PathVariable Long id,
			@RequestParam("amount") BigDecimal amount,
			@AuthenticationPrincipal User bidder,
			RedirectAttributes redirectAttributes) {

		try {
			offreService.placerOffre(id, bidder, amount);
			redirectAttributes.addFlashAttribute("successMessage", "Votre offre a été enregistrée.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/annonces/" + id;
	}
}

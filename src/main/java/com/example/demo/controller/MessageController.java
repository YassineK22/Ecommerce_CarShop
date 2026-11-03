package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.MessageService;
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
public class MessageController {

	@Autowired
	private MessageService messageService;

	/**
	 * Envoie un message au vendeur depuis la page détail de l'annonce.
	 */
	@PostMapping("/annonces/{id}/messages")
	public String envoyer(
			@PathVariable Long id,
			@RequestParam("contenu") String contenu,
			@AuthenticationPrincipal User sender,
			RedirectAttributes redirectAttributes) {

		try {
			messageService.envoyer(id, sender, contenu);
			redirectAttributes.addFlashAttribute("successMessage", "Message envoyé au vendeur.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		}
		return "redirect:/annonces/" + id;
	}

	/**
	 * Boîte de réception : messages reçus sur mes annonces.
	 * Affiche aussi les messages envoyés (sinon un message qu'on vient d'écrire semble perdu).
	 */
	@GetMapping("/mes-messages")
	public String boiteDeReception(@AuthenticationPrincipal User user, Model model) {
		model.addAttribute("messages", messageService.getRecus(user));
		model.addAttribute("envoyes", messageService.getEnvoyes(user));
		return "messages";
	}

	@PostMapping("/mes-messages/lu")
	public String marquerTousLus(
			@AuthenticationPrincipal User user,
			RedirectAttributes redirectAttributes) {
		messageService.marquerTousLus(user);
		redirectAttributes.addFlashAttribute("successMessage", "Tous les messages sont marqués comme lus.");
		return "redirect:/mes-messages";
	}
}

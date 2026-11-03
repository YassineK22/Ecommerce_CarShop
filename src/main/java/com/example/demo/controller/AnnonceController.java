package com.example.demo.controller;

import com.example.demo.dto.AnnonceForm;
import com.example.demo.dto.AnnonceSearchCriteria;
import com.example.demo.entity.Annonce;
import com.example.demo.entity.User;
import com.example.demo.service.AnnonceService;
import com.example.demo.service.FavoriService;
import com.example.demo.service.OffreService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/annonces")
public class AnnonceController {

	@Autowired
	private AnnonceService annonceService;
	@Autowired
	private OffreService offreService;
	@Autowired
	private FavoriService favoriService;

	@GetMapping
	public String list(
			@ModelAttribute AnnonceSearchCriteria criteria,
			@RequestParam(defaultValue = "0") int page,
			Model model) {
		Page<Annonce> resultat = annonceService.search(criteria, page);
		model.addAttribute("criteria", criteria);
		model.addAttribute("annonces", resultat.getContent());
		model.addAttribute("page", resultat);
		return "annonces/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		model.addAttribute("annonceForm", emptyAnnonceForm());
		model.addAttribute("formTitle", "Publier une annonce");
		model.addAttribute("submitLabel", "Publier");
		return "annonces/form";
	}

	@PostMapping
	public String create(
			@Valid @ModelAttribute("annonceForm") AnnonceForm form,
			BindingResult bindingResult,
			@AuthenticationPrincipal User seller,
			Model model,
			RedirectAttributes redirectAttributes) {

		validateAuction(form, bindingResult);

		if (!bindingResult.hasErrors()) {
			try {
				Annonce created = annonceService.create(form, seller);
				redirectAttributes.addFlashAttribute("successMessage", "Annonce publiée.");
				return "redirect:/annonces/" + created.getId();
			} catch (IllegalArgumentException ex) {
				bindingResult.reject("photos", ex.getMessage());
			} catch (IOException ex) {
				bindingResult.reject("photos", "Erreur lors de l'enregistrement des photos");
			}
		}

		model.addAttribute("formTitle", "Publier une annonce");
		model.addAttribute("submitLabel", "Publier");
		return "annonces/form";
	}

	@GetMapping("/{id}")
	public String detail(
			@PathVariable Long id,
			@AuthenticationPrincipal User currentUser,
			Model model) {
		Annonce annonce = annonceService.findById(id);
		model.addAttribute("annonce", annonce);
		model.addAttribute("canManage", currentUser != null && annonceService.canManage(annonce, currentUser));

		// M3 : offres de l'enchère
		model.addAttribute("offres", offreService.getOffres(id));
		BigDecimal meilleure = offreService.getMeilleureOffre(annonce);
		model.addAttribute("meilleureOffre", meilleure);
		model.addAttribute("montantMinimum", meilleure.add(new BigDecimal("0.01")));
		model.addAttribute("enchereOuverte",
				annonce.getStatutEnchere() == com.example.demo.entity.StatutEnchere.OUVERTE
						&& annonce.getAuctionEndDate() != null
						&& annonce.getAuctionEndDate().isAfter(LocalDateTime.now()));

		// M4 : état favori de l'utilisateur connecté
		model.addAttribute("isFavori", favoriService.isFavori(id, currentUser));
		return "annonces/detail";
	}

	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
		Annonce annonce = annonceService.findManagedById(id, user);
		AnnonceForm form = annonceService.formFromAnnonce(annonce);
		model.addAttribute("annonceForm", form);
		model.addAttribute("annonce", annonce);
		model.addAttribute("formTitle", "Modifier l'annonce");
		model.addAttribute("submitLabel", "Enregistrer");
		return "annonces/form";
	}

	@PostMapping("/{id}/edit")
	public String update(
			@PathVariable Long id,
			@Valid @ModelAttribute("annonceForm") AnnonceForm form,
			BindingResult bindingResult,
			@AuthenticationPrincipal User user,
			Model model,
			RedirectAttributes redirectAttributes) {

		Annonce annonce;
		try {
			annonce = annonceService.findManagedById(id, user);
		} catch (AccessDeniedException ex) {
			throw ex;
		}

		validateAuction(form, bindingResult);

		if (!bindingResult.hasErrors()) {
			try {
				annonceService.update(id, form, user);
				redirectAttributes.addFlashAttribute("successMessage", "Annonce mise à jour.");
				return "redirect:/annonces/" + id;
			} catch (IllegalArgumentException ex) {
				bindingResult.reject("photos", ex.getMessage());
			} catch (OptimisticLockingFailureException ex) {
				bindingResult.reject("concurrent", "Cette annonce a été modifiée entre temps, réessayez.");
			} catch (IOException ex) {
				bindingResult.reject("photos", "Erreur lors de l'enregistrement des photos");
			}
		}

		model.addAttribute("annonce", annonce);
		model.addAttribute("formTitle", "Modifier l'annonce");
		model.addAttribute("submitLabel", "Enregistrer");
		return "annonces/form";
	}

	@PostMapping("/{id}/delete")
	public String delete(
			@PathVariable Long id,
			@AuthenticationPrincipal User user,
			RedirectAttributes redirectAttributes) {
		try {
			annonceService.delete(id, user);
			redirectAttributes.addFlashAttribute("successMessage", "Annonce supprimée.");
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
			return "redirect:/annonces/" + id;
		} catch (IOException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", "Impossible de supprimer les fichiers photo.");
			return "redirect:/annonces/" + id;
		}
		return "redirect:/annonces";
	}

	private void validateAuction(AnnonceForm form, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return;
		}
		try {
			annonceService.validateAuctionRules(form);
		} catch (IllegalArgumentException ex) {
			bindingResult.rejectValue("auctionEndDate", "invalid", ex.getMessage());
		}
	}

	private AnnonceForm emptyAnnonceForm() {
		return new AnnonceForm(null, null, null, null, null, null, null, null, null, null);
	}
}

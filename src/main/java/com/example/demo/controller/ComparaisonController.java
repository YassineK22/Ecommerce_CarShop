package com.example.demo.controller;

import com.example.demo.service.ComparaisonService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bonus M4 : comparaison de 2 ou 3 annonces côte à côte.
 */
@Controller
public class ComparaisonController {

	@Autowired
	private ComparaisonService comparaisonService;

	@GetMapping("/comparer")
	public String comparer(
			@RequestParam(name = "ids", required = false) List<Long> ids,
			Model model,
			RedirectAttributes redirectAttributes) {

		try {
			model.addAttribute("annonces", comparaisonService.comparer(ids));
		} catch (IllegalArgumentException ex) {
			redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
			return "redirect:/annonces";
		}
		return "comparer";
	}
}

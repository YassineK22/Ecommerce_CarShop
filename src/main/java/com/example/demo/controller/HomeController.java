package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@Autowired
	private DashboardService dashboardService;

	@GetMapping("/")
	public String home(@AuthenticationPrincipal User user, Model model) {
		// Visiteur non connecté : page d'accueil publique
		if (user == null) {
			return "index";
		}
		// Utilisateur connecté : tableau de bord personnel
		model.addAttribute("dashboard", dashboardService.build(user));
		return "dashboard";
	}
}

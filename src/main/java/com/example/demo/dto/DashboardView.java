package com.example.demo.dto;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Favori;
import com.example.demo.entity.Message;
import com.example.demo.entity.Offre;
import java.util.List;

/**
 * Données affichées par le tableau de bord personnel.
 * Contient les compteurs, les listes courtes (5 éléments) et,
 * pour un administrateur, les statistiques d'administration.
 */
public record DashboardView(
		long mesAnnoncesActives,
		long mesOffres,
		long encheresGagnees,
		long mesFavoris,
		long messagesNonLus,
		List<Annonce> dernieresAnnonces,
		List<Offre> dernieresOffres,
		List<Message> derniersMessages,
		List<Favori> derniersFavoris,
		AdminStats adminStats) {

	/**
	 * Statistiques globales, affichées uniquement pour un administrateur.
	 */
	public record AdminStats(
			long utilisateurs,
			long annonces,
			long encheresOuvertes,
			long offres,
			long annoncesSignalees) {
	}
}

package com.example.demo.controller;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Offre;
import com.example.demo.service.AnnonceService;
import com.example.demo.service.OffreService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Petit endpoint REST pour la partie M3 : permet au front (plus tard)
 * de rafraîchir la meilleure offre sans recharger la page.
 */
@RestController
@RequestMapping("/api/annonces")
public class EnchereRestController {

	@Autowired
	private OffreService offreService;
	@Autowired
	private AnnonceService annonceService;

	public record MeilleureOffreView(
			Long annonceId,
			BigDecimal montant,
			String encherisseur,
			LocalDateTime date,
			LocalDateTime finEnchere,
			String statut) {
	}

	@GetMapping("/{id}/meilleure-offre")
	public MeilleureOffreView meilleureOffre(@PathVariable Long id) {
		Annonce annonce = annonceService.findById(id);
		List<Offre> offres = offreService.getOffres(id);

		if (offres.isEmpty()) {
			return new MeilleureOffreView(
					id,
					annonce.getPrice(),
					null,
					null,
					annonce.getAuctionEndDate(),
					annonce.getStatutEnchere() == null ? null : annonce.getStatutEnchere().name());
		}
		Offre meilleure = offres.get(0);
		return new MeilleureOffreView(
				id,
				meilleure.getAmount(),
				meilleure.getBidder().getName(),
				meilleure.getCreatedAt(),
				annonce.getAuctionEndDate(),
				annonce.getStatutEnchere() == null ? null : annonce.getStatutEnchere().name());
	}
}

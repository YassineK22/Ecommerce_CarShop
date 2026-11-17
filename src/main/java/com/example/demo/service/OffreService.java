package com.example.demo.service;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Offre;
import com.example.demo.entity.StatutEnchere;
import com.example.demo.entity.TypeAnnonce;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnonceRepository;
import com.example.demo.repository.OffreRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OffreService {

	@Autowired
	private OffreRepository offreRepository;
	@Autowired
	private AnnonceRepository annonceRepository;

	/**
	 * Liste des offres d'une annonce, de la plus élevée à la moins élevée.
	 */
	public List<Offre> getOffres(Long annonceId) {
		return offreRepository.findByAnnonceIdOrderByAmountDescCreatedAtDesc(annonceId);
	}

	/**
	 * Meilleure offre actuelle = offre max, ou le prix de départ si aucune offre.
	 */
	public BigDecimal getMeilleureOffre(Annonce annonce) {
		List<Offre> offres = getOffres(annonce.getId());
		return offres.isEmpty() ? annonce.getPrice() : offres.get(0).getAmount();
	}

	/**
	 * M5 : nombre total d'offres placées (tableau de bord).
	 */
	public long count() {
		return offreRepository.count();
	}

	/**
	 * Place une offre sur une enchère ouverte.
	 * Règles : annonce de type enchère, enchère encore ouverte,
	 * pas d'auto-enchère du vendeur, montant supérieur à la meilleure offre.
	 */
	@Transactional
	public Offre placerOffre(Long annonceId, User bidder, BigDecimal amount) {
		// Verrou pessimiste sur la ligne de l'annonce : deux offres simultanées
		// sont traitées l'une après l'autre, la deuxième voit la première.
		Annonce annonce = annonceRepository.findByIdForUpdate(annonceId)
				.orElseThrow(() -> new IllegalArgumentException("Annonce introuvable"));

		if (annonce.getType() != TypeAnnonce.ENCHERE) {
			throw new IllegalArgumentException("Cette annonce n'est pas une enchère");
		}
		if (annonce.getStatutEnchere() == null || annonce.getStatutEnchere() != StatutEnchere.OUVERTE) {
			throw new IllegalArgumentException("Cette enchère est clôturée");
		}
		if (annonce.getAuctionEndDate() == null || annonce.getAuctionEndDate().isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Cette enchère est clôturée");
		}
		if (annonce.getSeller().getId().equals(bidder.getId())) {
			throw new IllegalArgumentException("Vous ne pouvez pas enchérir sur votre propre annonce");
		}
		if (amount == null || amount.compareTo(getMeilleureOffre(annonce)) <= 0) {
			throw new IllegalArgumentException("L'offre doit être supérieure à la meilleure offre actuelle");
		}

		Offre offre = new Offre();
		offre.setAnnonce(annonce);
		offre.setBidder(bidder);
		offre.setAmount(amount);
		return offreRepository.save(offre);
	}
}

package com.example.demo.service;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Offre;
import com.example.demo.entity.StatutEnchere;
import com.example.demo.entity.TypeAnnonce;
import com.example.demo.repository.AnnonceRepository;
import com.example.demo.repository.OffreRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EnchereScheduler {

	private static final Logger log = LoggerFactory.getLogger(EnchereScheduler.class);

	@Autowired
	private AnnonceRepository annonceRepository;
	@Autowired
	private OffreRepository offreRepository;

	/**
	 * Clôture automatiquement les enchères expirées (toutes les 60 secondes).
	 * Pour chaque enchère expirée : affecte le gagnant (enchérisseur de la meilleure
	 * offre s'il y en a une) puis passe le statut à CLOTUREE.
	 */
	@Scheduled(fixedDelay = 60_000)
	@Transactional
	public void cloturerEncheresExpirees() {
		List<Annonce> expirees = annonceRepository.findByTypeAndStatutEnchereAndAuctionEndDateBefore(
				TypeAnnonce.ENCHERE, StatutEnchere.OUVERTE, LocalDateTime.now());

		for (Annonce annonce : expirees) {
			List<Offre> offres = offreRepository.findByAnnonceIdOrderByAmountDescCreatedAtDesc(annonce.getId());
			if (!offres.isEmpty()) {
				annonce.setWinner(offres.get(0).getBidder());
			}
			annonce.setStatutEnchere(StatutEnchere.CLOTUREE);
			annonceRepository.save(annonce);
			log.info("Enchère clôturée : annonce {} - {}", annonce.getId(), annonce.getTitle());
		}
	}
}

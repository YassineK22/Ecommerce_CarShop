package com.example.demo.service;

import com.example.demo.dto.DashboardView;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnonceRepository;
import com.example.demo.repository.FavoriRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.OffreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Prépare les données du tableau de bord personnel.
 * Uniquement des lectures : compteurs et 5 derniers éléments par catégorie.
 */
@Service
public class DashboardService {

	@Autowired
	private AnnonceRepository annonceRepository;
	@Autowired
	private OffreRepository offreRepository;
	@Autowired
	private FavoriRepository favoriRepository;
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private AnnonceService annonceService;
	@Autowired
	private OffreService offreService;

	public DashboardView build(User user) {
		Long userId = user.getId();

		return new DashboardView(
				annonceRepository.countBySellerIdAndActiveTrue(userId),
				offreRepository.countByBidderId(userId),
				offreRepository.countEncheresGagnees(userId),
				favoriRepository.countByUserId(userId),
				messageRepository.countByRecipientIdAndReadFalse(userId),
				annonceRepository.findTop5BySellerIdOrderByCreatedAtDesc(userId),
				offreRepository.findTop5ByBidderIdOrderByCreatedAtDesc(userId),
				messageRepository.findTop5ByRecipientIdOrderByReadAscSentAtDesc(userId),
				favoriRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId),
				adminStats(user));
	}

	/**
	 * Statistiques globales : seulement pour un administrateur, sinon null.
	 */
	private DashboardView.AdminStats adminStats(User user) {
		if (user.getRole() != Role.ADMIN) {
			return null;
		}
		return new DashboardView.AdminStats(
				userService.count(),
				annonceService.count(),
				annonceService.countEncheresOuvertes(),
				offreService.count(),
				annonceService.countSignalees());
	}
}

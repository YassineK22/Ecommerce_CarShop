package com.example.demo.service;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Favori;
import com.example.demo.entity.User;
import com.example.demo.repository.FavoriRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriService {

	@Autowired
	private FavoriRepository favoriRepository;
	@Autowired
	private AnnonceService annonceService;

	public boolean isFavori(Long annonceId, User user) {
		if (user == null) {
			return false;
		}
		return favoriRepository.existsByUserIdAndAnnonceId(user.getId(), annonceId);
	}

	public List<Favori> getFavoris(User user) {
		return favoriRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
	}

	/**
	 * Ajoute l'annonce aux favoris si elle n'y est pas, sinon la retire.
	 * Retourne true si l'annonce a été ajoutée, false si elle a été retirée.
	 */
	@Transactional
	public boolean basculer(Long annonceId, User user) {
		Annonce annonce = annonceService.findById(annonceId);

		return favoriRepository.findByUserIdAndAnnonceId(user.getId(), annonceId)
				.map(favori -> {
					favoriRepository.delete(favori);
					return false;
				})
				.orElseGet(() -> {
					Favori favori = new Favori();
					favori.setUser(user);
					favori.setAnnonce(annonce);
					favoriRepository.save(favori);
					return true;
				});
	}
}

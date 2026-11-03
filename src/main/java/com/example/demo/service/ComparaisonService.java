package com.example.demo.service;

import com.example.demo.entity.Annonce;
import com.example.demo.repository.AnnonceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Bonus M4 : comparaison de 2 ou 3 annonces.
 * Toute la règle métier (dédoublonnage, 2 à 3 annonces) est ici, pas dans le contrôleur.
 */
@Service
public class ComparaisonService {

	@Autowired
	private AnnonceRepository annonceRepository;

	public List<Annonce> comparer(List<Long> ids) {
		List<Long> idsUniques = ids == null ? List.of() : ids.stream().distinct().toList();
		if (idsUniques.size() < 2 || idsUniques.size() > 3) {
			throw new IllegalArgumentException("Sélectionnez 2 ou 3 annonces à comparer.");
		}

		List<Annonce> annonces = annonceRepository.findAllById(idsUniques);
		if (annonces.size() < 2) {
			throw new IllegalArgumentException("Sélectionnez 2 ou 3 annonces à comparer.");
		}
		return annonces;
	}
}

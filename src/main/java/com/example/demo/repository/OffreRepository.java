package com.example.demo.repository;

import com.example.demo.entity.Offre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OffreRepository extends JpaRepository<Offre, Long> {

	List<Offre> findByAnnonceIdOrderByAmountDescCreatedAtDesc(Long annonceId);

	boolean existsByAnnonceId(Long annonceId);

	/**
	 * M5 : suppression en cascade par l'administrateur (les offres bloquent la suppression d'une annonce).
	 */
	void deleteByAnnonceId(Long annonceId);

	/**
	 * Tableau de bord : nombre d'offres placées par un utilisateur.
	 */
	long countByBidderId(Long bidderId);

	/**
	 * Tableau de bord : les 5 dernières offres placées par un utilisateur.
	 */
	List<Offre> findTop5ByBidderIdOrderByCreatedAtDesc(Long bidderId);

	/**
	 * Tableau de bord : nombre d'enchères ouvertes où l'offre de l'utilisateur
	 * est actuellement la meilleure.
	 */
	@Query("select count(distinct o.annonce.id) from Offre o "
			+ "where o.bidder.id = :bidderId "
			+ "and o.annonce.type = com.example.demo.entity.TypeAnnonce.ENCHERE "
			+ "and o.annonce.statutEnchere = com.example.demo.entity.StatutEnchere.OUVERTE "
			+ "and o.amount = (select max(o2.amount) from Offre o2 where o2.annonce.id = o.annonce.id)")
	long countEncheresGagnees(@Param("bidderId") Long bidderId);
}

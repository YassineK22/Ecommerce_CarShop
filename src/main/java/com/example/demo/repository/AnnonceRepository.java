package com.example.demo.repository;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.StatutEnchere;
import com.example.demo.entity.TypeAnnonce;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {

	List<Annonce> findByTypeAndStatutEnchereAndAuctionEndDateBefore(
			TypeAnnonce type, StatutEnchere statutEnchere, LocalDateTime date);

	/**
	 * M5 : liste d'administration, les annonces signalées remontent en premier.
	 */
	List<Annonce> findAllByOrderBySignaleeDescCreatedAtDesc();

	/**
	 * M5 : nombre d'annonces signalées (tableau de bord).
	 */
	long countBySignaleeTrue();

	/**
	 * Tableau de bord : nombre d'annonces actives d'un vendeur.
	 */
	long countBySellerIdAndActiveTrue(Long sellerId);

	/**
	 * Tableau de bord : les 5 dernières annonces d'un vendeur.
	 */
	List<Annonce> findTop5BySellerIdOrderByCreatedAtDesc(Long sellerId);

	/**
	 * M5 : nombre d'enchères encore ouvertes (tableau de bord).
	 */
	long countByTypeAndStatutEnchere(TypeAnnonce type, StatutEnchere statutEnchere);

	/**
	 * Charge l'annonce en verrouillant sa ligne : deux offres simultanées
	 * passent alors l'une après l'autre (la deuxième voit la première).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Annonce a where a.id = :id")
	Optional<Annonce> findByIdForUpdate(@Param("id") Long id);
}

package com.example.demo.service;

import com.example.demo.dto.AnnonceForm;
import com.example.demo.dto.AnnonceSearchCriteria;
import com.example.demo.entity.Annonce;
import com.example.demo.entity.Role;
import com.example.demo.entity.StatutEnchere;
import com.example.demo.entity.TypeAnnonce;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnonceRepository;
import com.example.demo.repository.AnnonceSpecifications;
import com.example.demo.repository.FavoriRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.OffreRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnnonceService {

	@Autowired
	private AnnonceRepository annonceRepository;
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private OffreRepository offreRepository;
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private FavoriRepository favoriRepository;

	/**
	 * Recherche paginée : 9 annonces par page, les plus récentes d'abord.
	 */
	public Page<Annonce> search(AnnonceSearchCriteria criteria, int page) {
		return annonceRepository.findAll(
				AnnonceSpecifications.fromCriteria(criteria),
				PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "createdAt")));
	}

	public Annonce findById(Long id) {
		Annonce annonce = annonceRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Annonce introuvable"));
		if (!annonce.isActive()) {
			throw new IllegalArgumentException("Annonce introuvable");
		}
		return annonce;
	}

	public boolean isOwner(Annonce annonce, User user) {
		return annonce.getSeller().getId().equals(user.getId());
	}

	public boolean canManage(Annonce annonce, User user) {
		return user.getRole() == Role.ADMIN || isOwner(annonce, user);
	}

	public Annonce findManagedById(Long id, User user) {
		Annonce annonce = annonceRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Annonce introuvable"));
		if (!canManage(annonce, user)) {
			throw new AccessDeniedException("Vous ne pouvez modifier que vos propres annonces");
		}
		return annonce;
	}

	@Transactional
	public Annonce create(AnnonceForm form, User seller) throws IOException {
		List<String> photoPaths = fileStorageService.storePhotos(form.photos());
		if (photoPaths.isEmpty()) {
			throw new IllegalArgumentException("Ajoutez au moins une photo");
		}

		Annonce annonce = mapFormToEntity(form, new Annonce());
		applyAuctionFields(form, annonce);
		annonce.setSeller(seller);
		annonce.setPhotos(photoPaths);
		annonce.setCreatedAt(LocalDateTime.now());
		annonce.setActive(true);
		return annonceRepository.save(annonce);
	}

	@Transactional
	public Annonce update(Long id, AnnonceForm form, User user) throws IOException {
		Annonce annonce = findManagedById(id, user);
		mapFormToEntity(form, annonce);
		applyAuctionFields(form, annonce);

		List<String> newPhotos = fileStorageService.storePhotos(form.photos());
		if (!newPhotos.isEmpty()) {
			annonce.getPhotos().addAll(newPhotos);
		}
		if (annonce.getPhotos().isEmpty()) {
			throw new IllegalArgumentException("L'annonce doit contenir au moins une photo");
		}

		return annonceRepository.save(annonce);
	}

	@Transactional
	public void delete(Long id, User user) throws IOException {
		Annonce annonce = findManagedById(id, user);

		// Les offres et les messages sont des données liées : on refuse la suppression
		if (offreRepository.existsByAnnonceId(id) || messageRepository.existsByAnnonceId(id)) {
			throw new IllegalArgumentException(
					"Suppression impossible : cette annonce a reçu des offres ou des messages.");
		}
		// Les favoris ne sont que des signets : ils partent avec l'annonce
		favoriRepository.deleteByAnnonceId(id);

		deleteAnnonceAndPhotos(annonce);
	}

	/**
	 * M5 : liste d'administration, les annonces signalées en premier.
	 */
	public List<Annonce> findAllForAdmin() {
		return annonceRepository.findAllByOrderBySignaleeDescCreatedAtDesc();
	}

	/**
	 * M5 : un utilisateur signale une annonce (annonce d'un autre vendeur).
	 */
	@Transactional
	public Annonce signaler(Long id, User reporter) {
		Annonce annonce = findById(id);
		if (isOwner(annonce, reporter)) {
			throw new IllegalArgumentException("Vous ne pouvez pas signaler votre propre annonce");
		}
		annonce.setSignalee(true);
		return annonceRepository.save(annonce);
	}

	/**
	 * M5 : suppression d'une annonce par l'administrateur.
	 * Contrairement au vendeur, l'admin peut tout supprimer : on efface d'abord
	 * les données liées (offres, messages, favoris) pour éviter une erreur de clé étrangère.
	 */
	@Transactional
	public void deleteByAdmin(Long id) throws IOException {
		Annonce annonce = annonceRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Annonce introuvable"));

		offreRepository.deleteByAnnonceId(id);
		messageRepository.deleteByAnnonceId(id);
		favoriRepository.deleteByAnnonceId(id);

		deleteAnnonceAndPhotos(annonce);
	}

	/**
	 * M5 : compteurs du tableau de bord.
	 */
	public long count() {
		return annonceRepository.count();
	}

	public long countSignalees() {
		return annonceRepository.countBySignaleeTrue();
	}

	public long countEncheresOuvertes() {
		return annonceRepository.countByTypeAndStatutEnchere(TypeAnnonce.ENCHERE, StatutEnchere.OUVERTE);
	}

	/**
	 * Suppression de l'annonce puis de ses fichiers photo (appelé seulement après le flush,
	 * pour ne pas perdre les images si la suppression en base échoue).
	 */
	private void deleteAnnonceAndPhotos(Annonce annonce) throws IOException {
		annonceRepository.delete(annonce);
		annonceRepository.flush();

		// Les fichiers ne sont supprimés qu'une fois la suppression en base réussie
		for (String photo : annonce.getPhotos()) {
			fileStorageService.deletePhoto(photo);
		}
	}

	public AnnonceForm formFromAnnonce(Annonce annonce) {
		return new AnnonceForm(
				annonce.getTitle(),
				annonce.getDescription(),
				annonce.getMake(),
				annonce.getModel(),
				annonce.getYear(),
				annonce.getMileage(),
				annonce.getPrice(),
				annonce.getType(),
				annonce.getAuctionEndDate(),
				null);
	}

	public void validateAuctionRules(AnnonceForm form) {
		if (form.type() == TypeAnnonce.ENCHERE) {
			if (form.auctionEndDate() == null) {
				throw new IllegalArgumentException("La date de fin est obligatoire pour une enchère");
			}
			if (!form.auctionEndDate().isAfter(LocalDateTime.now())) {
				throw new IllegalArgumentException("La date de fin doit être dans le futur");
			}
		}
	}

	private Annonce mapFormToEntity(AnnonceForm form, Annonce annonce) {
		annonce.setTitle(form.title().trim());
		annonce.setDescription(form.description().trim());
		annonce.setMake(form.make().trim());
		annonce.setModel(form.model().trim());
		annonce.setYear(form.year());
		annonce.setMileage(form.mileage());
		annonce.setPrice(form.price());
		annonce.setType(form.type());
		return annonce;
	}

	private void applyAuctionFields(AnnonceForm form, Annonce annonce) {
		if (form.type() == TypeAnnonce.ENCHERE) {
			annonce.setAuctionEndDate(form.auctionEndDate());
			if (annonce.getStatutEnchere() == null) {
				annonce.setStatutEnchere(StatutEnchere.OUVERTE);
			}
		} else {
			annonce.setAuctionEndDate(null);
			annonce.setStatutEnchere(null);
			annonce.setWinner(null);
		}
	}
}

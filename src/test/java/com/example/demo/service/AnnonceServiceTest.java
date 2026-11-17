package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.AnnonceForm;
import com.example.demo.dto.AnnonceSearchCriteria;
import com.example.demo.entity.Annonce;
import com.example.demo.entity.TypeAnnonce;
import com.example.demo.entity.User;
import com.example.demo.repository.AnnonceRepository;
import com.example.demo.repository.FavoriRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.OffreRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

/**
 * TP5 - Tache 2 : tests du service AnnonceService (CRUD complet).
 *
 * JUnit 5 + Mockito uniquement : les depots sont simules (@Mock),
 * aucune base de donnees n'est utilisee.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

	@Mock
	private AnnonceRepository annonceRepository;
	@Mock
	private FileStorageService fileStorageService;
	@Mock
	private OffreRepository offreRepository;
	@Mock
	private MessageRepository messageRepository;
	@Mock
	private FavoriRepository favoriRepository;

	@InjectMocks
	private AnnonceService annonceService;

	private User vendeur;
	private Annonce annonce;

	@BeforeEach
	void setUp() {
		vendeur = new User();
		vendeur.setId(1L);
		vendeur.setName("Hamza Vendeur");
		vendeur.setEmail("hamza@autohub.tn");

		annonce = new Annonce();
		annonce.setId(1L);
		annonce.setTitle("Renault Clio 5 Intens");
		annonce.setDescription("Belle voiture");
		annonce.setMake("Renault");
		annonce.setModel("Clio 5");
		annonce.setYear(2023);
		annonce.setMileage(26000);
		annonce.setPrice(new BigDecimal("62000.00"));
		annonce.setType(TypeAnnonce.PRIX_FIXE);
		annonce.setSeller(vendeur);
		annonce.setPhotos(List.of("/uploads/photo1.jpg"));
		annonce.setActive(true);
	}

	private AnnonceForm formulaireValide() {
		return new AnnonceForm(
				"Renault Clio 5 Intens",
				"Belle voiture",
				"Renault",
				"Clio 5",
				2023,
				26000,
				new BigDecimal("62000.00"),
				TypeAnnonce.PRIX_FIXE,
				null,
				null);
	}

	// ------------------------------------------------------------------
	// CREATE
	// ------------------------------------------------------------------

	@Test
	void creerEnregistreLEtatEtSauvegarde() throws IOException {
		when(fileStorageService.storePhotos(any())).thenReturn(List.of("/uploads/photo1.jpg"));
		when(annonceRepository.save(any(Annonce.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Annonce cree = annonceService.create(formulaireValide(), vendeur);

		// L'entite renvoyee contient les donnees du formulaire
		assertEquals("Renault Clio 5 Intens", cree.getTitle());
		assertEquals(new BigDecimal("62000.00"), cree.getPrice());
		assertEquals(vendeur, cree.getSeller());
		assertEquals(List.of("/uploads/photo1.jpg"), cree.getPhotos());
		assertTrue(cree.isActive());

		// Le depot a bien ete appele
		verify(annonceRepository).save(any(Annonce.class));
	}

	@Test
	void creerSansPhoto_leveUneExceptionEtNeSauvegardePas() throws IOException {
		when(fileStorageService.storePhotos(any())).thenReturn(List.of());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.create(formulaireValide(), vendeur));

		assertEquals("Ajoutez au moins une photo", exception.getMessage());
		verify(annonceRepository, never()).save(any(Annonce.class));
	}

	// ------------------------------------------------------------------
	// READ ALL
	// ------------------------------------------------------------------

	@Test
	void lireToutesLesAnnonces_retourneLaPage() {
		Page<Annonce> page = new PageImpl<>(List.of(annonce));
		when(annonceRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(page);

		Page<Annonce> resultat = annonceService.search(new AnnonceSearchCriteria(null, null, null, null, null, null), 0);

		assertEquals(1, resultat.getContent().size());
		assertEquals("Renault Clio 5 Intens", resultat.getContent().get(0).getTitle());
	}

	@Test
	void lireToutesLesAnnonces_pageVide() {
		Page<Annonce> page = new PageImpl<>(List.of());
		when(annonceRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(page);

		Page<Annonce> resultat = annonceService.search(new AnnonceSearchCriteria(null, null, null, null, null, null), 0);

		assertTrue(resultat.getContent().isEmpty());
		assertEquals(0, resultat.getTotalElements());
	}

	// ------------------------------------------------------------------
	// READ BY ID
	// ------------------------------------------------------------------

	@Test
	void lireUneAnnonce_parId_annonceTrouvee() {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

		Annonce resultat = annonceService.findById(1L);

		assertEquals(annonce, resultat);
		assertEquals(1L, resultat.getId());
	}

	@Test
	void lireUneAnnonce_parId_annonceIntrouvable_leveUneException() {
		when(annonceRepository.findById(999L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.findById(999L));

		assertEquals("Annonce introuvable", exception.getMessage());
	}

	@Test
	void lireUneAnnonce_parId_annonceDesactivee_leveUneException() {
		annonce.setActive(false);
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.findById(1L));

		assertEquals("Annonce introuvable", exception.getMessage());
	}

	// ------------------------------------------------------------------
	// UPDATE
	// ------------------------------------------------------------------

	@Test
	void modifierChargeModifieEtSauvegardeLEtat() throws IOException {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
		when(fileStorageService.storePhotos(any())).thenReturn(List.of());
		when(annonceRepository.save(any(Annonce.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AnnonceForm formulaire = new AnnonceForm(
				"Renault Clio 5 Intens Rouge",
				"Mise a jour de la description",
				"Renault",
				"Clio 5",
				2023,
				26000,
				new BigDecimal("61000.00"),
				TypeAnnonce.PRIX_FIXE,
				null,
				null);

		Annonce modifie = annonceService.update(1L, formulaire, vendeur);

		// L'entite a bien ete modifiee puis sauvegardee
		assertEquals("Renault Clio 5 Intens Rouge", modifie.getTitle());
		assertEquals("Mise a jour de la description", modifie.getDescription());
		assertEquals(new BigDecimal("61000.00"), modifie.getPrice());
		verify(annonceRepository).save(annonce);
	}

	@Test
	void modifierAnnonceIntrouvable_leveUneException() {
		when(annonceRepository.findById(999L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.update(999L, formulaireValide(), vendeur));

		assertEquals("Annonce introuvable", exception.getMessage());
		verify(annonceRepository, never()).save(any(Annonce.class));
	}

	@Test
	void modifierAnnonceDunAutreVendeur_leveUneAccessDeniedException() {
		User autre = new User();
		autre.setId(2L);
		autre.setEmail("ahmed@autohub.tn");

		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

		assertThrows(AccessDeniedException.class,
				() -> annonceService.update(1L, formulaireValide(), autre));
		verify(annonceRepository, never()).save(any(Annonce.class));
	}

	// ------------------------------------------------------------------
	// DELETE
	// ------------------------------------------------------------------

	@Test
	void supprimerSupprimeLEtatEtLesFichiersPhoto() throws IOException {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
		when(offreRepository.existsByAnnonceId(1L)).thenReturn(false);
		when(messageRepository.existsByAnnonceId(1L)).thenReturn(false);

		annonceService.delete(1L, vendeur);

		verify(favoriRepository).deleteByAnnonceId(1L);
		verify(annonceRepository).delete(annonce);
		verify(annonceRepository).flush();
		verify(fileStorageService).deletePhoto("/uploads/photo1.jpg");
	}

	@Test
	void supprimerAnnonceAvecOffres_refuseLaSuppression() {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
		when(offreRepository.existsByAnnonceId(1L)).thenReturn(true);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.delete(1L, vendeur));

		assertTrue(exception.getMessage().contains("Suppression impossible"));
		verify(annonceRepository, never()).delete(any(Annonce.class));
	}

	@Test
	void supprimerAnnonceIntrouvable_leveUneException() {
		when(annonceRepository.findById(999L)).thenReturn(Optional.empty());

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.delete(999L, vendeur));

		assertEquals("Annonce introuvable", exception.getMessage());
		verify(annonceRepository, never()).delete(any(Annonce.class));
	}

	// ------------------------------------------------------------------
	// M5 : signalement et suppression par l'administrateur
	// ------------------------------------------------------------------

	@Test
	void signalerMarqueLAnnonceCommeSignalee() {
		User autreUtilisateur = new User();
		autreUtilisateur.setId(2L);

		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
		when(annonceRepository.save(any(Annonce.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Annonce signalee = annonceService.signaler(1L, autreUtilisateur);

		assertTrue(signalee.isSignalee());
		verify(annonceRepository).save(annonce);
	}

	@Test
	void signalerSaPropreAnnonce_leveUneException() {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> annonceService.signaler(1L, vendeur));

		assertFalse(annonce.isSignalee());
		assertTrue(exception.getMessage().contains("propre annonce"));
	}

	@Test
	void supprimerParAdminSupprimeLesDonneesLiees() throws IOException {
		when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

		annonceService.deleteByAdmin(1L);

		// Les enfants sont supprimes d'abord pour eviter une erreur de cle etrangere
		verify(offreRepository).deleteByAnnonceId(1L);
		verify(messageRepository).deleteByAnnonceId(1L);
		verify(favoriRepository).deleteByAnnonceId(1L);
		verify(annonceRepository).delete(annonce);
		verify(fileStorageService).deletePhoto("/uploads/photo1.jpg");
	}
}

package com.example.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.demo.entity.Annonce;
import com.example.demo.entity.Role;
import com.example.demo.entity.TypeAnnonce;
import com.example.demo.entity.User;
import com.example.demo.service.AnnonceService;
import com.example.demo.service.FavoriService;
import com.example.demo.service.OffreService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * TP5 - Tache 1 : tests du controleur AnnonceController (CRUD complet).
 *
 * Le controleur renvoie des vues Thymeleaf et des redirections : on verifie donc
 * le statut HTTP, le nom de la vue, la redirection et le contenu du modele.
 * Le service est simule (@MockitoBean), aucune base de donnees n'est utilisee.
 */
@WebMvcTest(AnnonceController.class)
class AnnonceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	// Le service du controleur est simule : on teste seulement le controleur.
	@MockitoBean
	private AnnonceService annonceService;
	@MockitoBean
	private OffreService offreService;
	@MockitoBean
	private FavoriService favoriService;

	private User vendeur;
	private Annonce annonce;

	@BeforeEach
	void setUp() {
		vendeur = new User();
		vendeur.setId(1L);
		vendeur.setName("Hamza Vendeur");
		vendeur.setEmail("hamza@autohub.tn");
		vendeur.setPassword("hash");
		vendeur.setRole(Role.UTILISATEUR);
		vendeur.setActive(true);

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
		annonce.setCreatedAt(LocalDateTime.now());
		annonce.setActive(true);
	}

	// ------------------------------------------------------------------
	// CREATE
	// ------------------------------------------------------------------

	@Test
	void creerAnnonce_succes_redirigeVersLaPageDeDetail() throws Exception {
		when(annonceService.create(any(), eq(vendeur))).thenReturn(annonce);

		mockMvc.perform(post("/annonces")
						.with(user(vendeur))
						.with(csrf())
						.param("title", "Renault Clio 5 Intens")
						.param("description", "Belle voiture")
						.param("make", "Renault")
						.param("model", "Clio 5")
						.param("year", "2023")
						.param("mileage", "26000")
						.param("price", "62000.00")
						.param("type", "PRIX_FIXE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/annonces/1"))
				.andExpect(flash().attributeExists("successMessage"));

		verify(annonceService).create(any(), eq(vendeur));
	}

	@Test
	void creerAnnonce_erreurDuService_resteSurLeFormulaire() throws Exception {
		when(annonceService.create(any(), eq(vendeur)))
				.thenThrow(new IllegalArgumentException("Ajoutez au moins une photo"));

		mockMvc.perform(post("/annonces")
						.with(user(vendeur))
						.with(csrf())
						.param("title", "Renault Clio 5 Intens")
						.param("description", "Belle voiture")
						.param("make", "Renault")
						.param("model", "Clio 5")
						.param("year", "2023")
						.param("mileage", "26000")
						.param("price", "62000.00")
						.param("type", "PRIX_FIXE"))
				.andExpect(status().isOk())
				.andExpect(view().name("annonces/form"))
				.andExpect(model().attributeHasErrors("annonceForm"));

		verify(annonceService).create(any(), eq(vendeur));
	}

	// ------------------------------------------------------------------
	// READ ALL
	// ------------------------------------------------------------------

	@Test
	void lireToutesLesAnnonces_succes_afficheLaListe() throws Exception {
		Page<Annonce> page = new PageImpl<>(List.of(annonce));
		when(annonceService.search(any(), eq(0))).thenReturn(page);

		mockMvc.perform(get("/annonces").with(user(vendeur)))
				.andExpect(status().isOk())
				.andExpect(view().name("annonces/list"))
				.andExpect(model().attribute("annonces", hasSize(1)))
				.andExpect(model().attributeExists("page"))
				.andExpect(model().attributeExists("criteria"));
	}

	@Test
	void lireToutesLesAnnonces_listeVide_afficheLaListeSansAnnonce() throws Exception {
		Page<Annonce> page = new PageImpl<>(List.of());
		when(annonceService.search(any(), eq(0))).thenReturn(page);

		mockMvc.perform(get("/annonces").with(user(vendeur)))
				.andExpect(status().isOk())
				.andExpect(view().name("annonces/list"))
				.andExpect(model().attribute("annonces", hasSize(0)));
	}

	// ------------------------------------------------------------------
	// READ BY ID
	// ------------------------------------------------------------------

	@Test
	void lireUneAnnonce_parId_succes_afficheLeDetail() throws Exception {
		when(annonceService.findById(1L)).thenReturn(annonce);
		when(annonceService.canManage(annonce, vendeur)).thenReturn(false);
		when(offreService.getOffres(1L)).thenReturn(List.of());
		when(offreService.getMeilleureOffre(annonce)).thenReturn(new BigDecimal("62000.00"));
		when(favoriService.isFavori(1L, vendeur)).thenReturn(false);

		mockMvc.perform(get("/annonces/{id}", 1L).with(user(vendeur)))
				.andExpect(status().isOk())
				.andExpect(view().name("annonces/detail"))
				.andExpect(model().attribute("annonce", annonce))
				.andExpect(model().attributeExists("offres"))
				.andExpect(model().attributeExists("meilleureOffre"));
	}

	@Test
	void lireUneAnnonce_parId_annonceInexistante_leveUneException() throws Exception {
		when(annonceService.findById(999L)).thenThrow(new IllegalArgumentException("Annonce introuvable"));

		Exception exception = assertThrows(Exception.class,
				() -> mockMvc.perform(get("/annonces/{id}", 999L).with(user(vendeur))));

		// Le controleur ne masque pas l'erreur : le service leve "Annonce introuvable".
		assertTrue(
				contientCause(exception, IllegalArgumentException.class, "Annonce introuvable"),
				"L'exception attendue (Annonce introuvable) n'a pas ete levee : " + exception);
	}

	// ------------------------------------------------------------------
	// UPDATE
	// ------------------------------------------------------------------

	@Test
	void modifierAnnonce_succes_redirigeVersLeDetail() throws Exception {
		when(annonceService.findManagedById(1L, vendeur)).thenReturn(annonce);
		when(annonceService.update(eq(1L), any(), eq(vendeur))).thenReturn(annonce);

		mockMvc.perform(post("/annonces/{id}/edit", 1L)
						.with(user(vendeur))
						.with(csrf())
						.param("title", "Renault Clio 5 Intens Rouge")
						.param("description", "Belle voiture")
						.param("make", "Renault")
						.param("model", "Clio 5")
						.param("year", "2023")
						.param("mileage", "26000")
						.param("price", "62000.00")
						.param("type", "PRIX_FIXE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/annonces/1"));

		verify(annonceService).update(eq(1L), any(), eq(vendeur));
	}

	@Test
	void modifierAnnonce_annonceInexistante_leveUneException() throws Exception {
		annonceIntrouvable();

		Exception exception = assertThrows(Exception.class,
				() -> mockMvc.perform(post("/annonces/{id}/edit", 999L)
						.with(user(vendeur))
						.with(csrf())
						.param("title", "Titre")
						.param("description", "Description")
						.param("make", "Renault")
						.param("model", "Clio 5")
						.param("year", "2023")
						.param("mileage", "26000")
						.param("price", "62000.00")
						.param("type", "PRIX_FIXE")));

		assertTrue(
				contientCause(exception, IllegalArgumentException.class, "Annonce introuvable"),
				"L'exception attendue (Annonce introuvable) n'a pas ete levee : " + exception);

		verify(annonceService, never()).update(any(), any(), any());
	}

	// ------------------------------------------------------------------
	// DELETE
	// ------------------------------------------------------------------

	@Test
	void supprimerAnnonce_succes_redirigeVersLaListe() throws Exception {
		mockMvc.perform(post("/annonces/{id}/delete", 1L)
						.with(user(vendeur))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/annonces"));

		// On verifie que la methode du service a bien ete appelee.
		verify(annonceService).delete(eq(1L), eq(vendeur));
	}

	@Test
	void supprimerAnnonce_erreurDuService_redirigeAvecUnMessage() throws Exception {
		doThrow(new IllegalArgumentException("Suppression impossible"))
				.when(annonceService).delete(eq(1L), eq(vendeur));

		mockMvc.perform(post("/annonces/{id}/delete", 1L)
						.with(user(vendeur))
						.with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/annonces/1"))
				.andExpect(flash().attributeExists("errorMessage"));
	}

	// ------------------------------------------------------------------
	// Utilitaires
	// ------------------------------------------------------------------

	/** Simule une annonce introuvable pour le cas "update". */
	private void annonceIntrouvable() {
		when(annonceService.findManagedById(eq(999L), any()))
				.thenThrow(new IllegalArgumentException("Annonce introuvable"));
	}

	/** Cherche dans toute la chaine des causes une exception du type attendu. */
	private static boolean contientCause(Throwable exception, Class<?> type, String message) {
		Throwable current = exception;
		while (current != null) {
			if (type.isInstance(current) && current.getMessage() != null
					&& current.getMessage().contains(message)) {
				return true;
			}
			current = current.getCause();
		}
		return false;
	}
}

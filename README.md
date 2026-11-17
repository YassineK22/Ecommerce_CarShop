# AutoHub

Marketplace de voitures d'occasion : publication d'annonces, **enchères**, favoris, messagerie
entre utilisateurs et **administration**. Application **monolithique** Spring Boot (Java 17) dont les
pages sont rendues côté serveur par **Thymeleaf**, avec une base **PostgreSQL**.

| Élément | Version / choix |
|---|---|
| Framework | Spring Boot **4.1.1** (Spring MVC, Spring Security, Spring Data JPA) |
| Langage | Java **17** |
| Build | Maven (wrapper `mvnw` / `mvnw.cmd` inclus) |
| Vues | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| Base | **PostgreSQL** — aucune base en mémoire, pas de H2 |
| API | springdoc-openapi **3.2.0** (Swagger UI) |
| Divers | Lombok, BCrypt pour les mots de passe, Hibernate `ddl-auto=update` |

---

## Architecture

**Monolithe** : un seul module Maven, une seule application déployée (un jar), une seule base de
données. Pas de microservices, pas d'API séparée : les pages HTML et le peu de REST vivent dans la
même application.

```
Navigateur
    │  HTTP (formulaires HTML + un endpoint JSON)
    ▼
Controller  ──►  Service  ──►  Repository  ──►  PostgreSQL
 (controller/)    (service/)    (repository/)
    │                │
    ▼                ▼
Vue Thymeleaf   Entités JPA
(templates/)       (entity/)
```

| Package | Rôle |
|---|---|
| `com.example.demo.controller` | Points d'entrée HTTP : contrôleurs **MVC** (rendent une vue Thymeleaf) et un contrôleur **REST** (JSON) |
| `com.example.demo.service` | Logique métier : règles des enchères, suppression sûre, dashboard, stockage des photos, scheduler de clôture |
| `com.example.demo.repository` | Interfaces Spring Data JPA (requêtes dérivées + quelques `@Query`) |
| `com.example.demo.entity` | Entités JPA : `User`, `Annonce`, `Offre`, `Favori`, `Message`, `Role`, `TypeAnnonce`, `StatutEnchere` |
| `com.example.demo.dto` | Formulaires (`AnnonceForm`, `RegistrationForm`, `ProfileForm`), critères de recherche et `DashboardView` |
| `com.example.demo.config` | `SecurityConfig` (règles d'accès), `WebMvcConfig` (fichiers `/uploads/**`), `AdminInitializer` (compte admin) |

### Vues Thymeleaf

| Dossier / fichier | Contenu |
|---|---|
| `templates/fragments/layout.html` | `<head>` et en-tête de navigation partagés par toutes les pages |
| `templates/index.html` | Accueil public (visiteur non connecté) |
| `templates/dashboard.html` | Tableau de bord de l'utilisateur connecté (`/`) |
| `templates/annonces/list.html`, `detail.html`, `form.html` | Liste/recherche, détail (enchère, favori, message), création/édition |
| `templates/favoris.html`, `messages.html`, `profile.html`, `comparer.html` | Favoris, messagerie, profil, comparaison |
| `templates/login.html`, `register.html` | Connexion / inscription |
| `templates/admin/dashboard.html`, `annonces.html`, `utilisateurs.html` | Administration |
| `static/css/style.css` | Feuille de style unique |

### Pages principales (MVC)

| URL | Page | Accès |
|---|---|---|
| `/` | Accueil public **ou** tableau de bord si connecté | tous |
| `/annonces`, `/annonces/{id}` | Liste + recherche, détail d'une annonce | public |
| `/comparer` | Comparaison de 2 annonces | public |
| `/annonces/new`, `/annonces/{id}/edit` | Publier / modifier une annonce | connecté (propriétaire ou admin pour l'édition) |
| `/mes-favoris`, `/mes-messages`, `/profile` | Espace personnel | connecté |
| `/admin`, `/admin/annonces`, `/admin/utilisateurs` | Administration | rôle **ADMIN** |

### Endpoints REST

Le projet expose **un seul endpoint JSON** ; tout le reste est constitué de formulaires HTML
(contrôleurs MVC), raison pour laquelle Swagger n'affiche qu'une opération.

| Méthode | URL | Réponse | Accès |
|---|---|---|---|
| `GET` | `/api/annonces/{id}/meilleure-offre` | JSON `MeilleureOffreView` : montant de la meilleure offre (ou prix de départ), enchérisseur, date, fin d'enchère, statut | public |

**Swagger UI : <http://localhost:8080/swagger-ui.html>** — JSON OpenAPI : <http://localhost:8080/v3/api-docs>

---

## Prérequis

- **JDK 17** (ou plus récent ; le projet compile en `release 17`).
- **PostgreSQL** installé et démarré.
- Rien d'autre : Maven est fourni par le wrapper (`mvnw.cmd` sous Windows, `./mvnw` sous Linux/macOS).

## Base de données PostgreSQL

L'application **n'utilise pas** H2 ni une autre base en mémoire : les données doivent survivre aux
redémarrages.

### 1. Créer la base

Avec **pgAdmin** ou **psql** :

```sql
CREATE DATABASE autohub;
```

### 2. Configurer la connexion avec des variables d'environnement

`application.properties` lit ces variables (`${VARIABLE:valeur_par_défaut}`), **aucun mot de passe
n'est écrit dans le code** :

```powershell
$env:SPRING_DATASOURCE_URL      = "jdbc:postgresql://localhost:5432/autohub"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "votre_mot_de_passe"
```

| Variable | Valeur par défaut | Rôle |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/autohub` | URL JDBC |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Utilisateur PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | *(vide)* | Mot de passe PostgreSQL |

**Option B — fichier local (gitignored)** : copier
`src/main/resources/application-local.properties.example` en
`src/main/resources/application-local.properties`, puis renseigner les mêmes valeurs. Le fichier est déjà importé par `application.properties`
(`spring.config.import=optional:classpath:application-local.properties`).

### 3. Schéma

`spring.jpa.hibernate.ddl-auto=update` : Hibernate **crée et met à jour** les tables et colonnes au
démarrage, sans effacer les données existantes.

### Erreur de connexion

Si PostgreSQL n'est pas démarré ou si la base n'existe pas, le démarrage échoue avec une erreur du
type *Connection refused* ou *database "autohub" does not exist*. Corrigez le service, la base,
l'URL ou le mot de passe — il n'y a **pas** de repli H2.

---

## Lancer l'application

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"   # adapter au chemin réel
.\mvnw.cmd spring-boot:run
```

L'application démarre en quelques secondes sur **<http://localhost:8080>**.

- Les photos sont stockées dans le dossier `uploads/` (configurable avec `app.upload.dir`).
- Il n'y a **pas** de devtools : après une modification d'un template ou d'une classe, il faut
  **redémarrer** l'application (`Ctrl+C` puis relancer la commande).

---

## Compte administrateur

Le compte admin est **créé au démarrage** par `AdminInitializer` (un `CommandLineRunner`),
uniquement s'il n'existe pas déjà. L'email et le mot de passe viennent de l'environnement — **aucun
mot de passe n'est codé en dur** :

```powershell
$env:APP_ADMIN_EMAIL    = "admin@autohub.tn"
$env:APP_ADMIN_PASSWORD = "votre-mot-de-passe"
$env:APP_ADMIN_NAME     = "Administrateur"      # optionnel
.\mvnw.cmd spring-boot:run
```

Sans ces variables, l'application démarre normalement mais **aucun** compte administrateur n'est créé
(un avertissement est écrit dans les logs).

Les accès `/admin/**` sont réservés au rôle `ADMIN` (`SecurityConfig`) ; un administrateur ne peut pas
désactiver son propre compte.

---

## Comptes de démonstration

Les trois comptes ont été créés par le formulaire d'inscription (mot de passe hashé en BCrypt).
**Mot de passe identique pour les trois : `Passer1234`**

| Nom affiché | Email | Mot de passe | Rôle |
|-------------|-------|--------------|------|
| Hamza Vendeur | `hamza@autohub.tn` | `Passer1234` | Vendeur particulier — dépose des **enchères** |
| Ahmed Encherisseur | `ahmed@autohub.tn` | `Passer1234` | Acheteur — enchérit, met en favori, envoie des messages |
| AutoPlus Tunisie | `autoplus@autohub.tn` | `Passer1234` | Concessionnaire — dépose des annonces à **prix fixe** (neuves et occasions) |

### Scénario de démonstration

1. Se connecter avec **ahmed@autohub.tn** → parcourir les annonces, ajouter aux favoris, comparer 2 annonces, envoyer un message.
2. Sur une annonce **ENCHÈRE** (Golf 7, Seat Ibiza) : placer une offre. Le bloc « Enchères » n'apparaît **pas** sur les annonces à prix fixe.
3. Sur les annonces **CLÔTURÉES** de Hamza (Peugeot 208, Dacia Sandero) : l'enchère est terminée, les offres et le gagnant s'affichent.
4. Se connecter avec **autoplus@autohub.tn** → publier une annonce à prix fixe avec photo.
5. Signaler une annonce (bouton 🚩) puis se connecter en **administrateur** → la page `/admin/annonces`
   affiche l'annonce signalée en premier, le tableau de bord donne les compteurs, et `/admin/utilisateurs`
   permet de désactiver un compte (qui ne peut plus se connecter).
6. Se connecter avec n'importe quel compte → la page `/` devient le **tableau de bord personnel**
   (mes annonces, mes offres, enchères gagnées, favoris, messages non lus + les 5 derniers de chaque).

### Contenu de la base de démonstration

12 annonces : **8 prix fixe** (AutoPlus Tunisie — 3 neuves 2026 / 0 km et 5 occasions) et
**4 enchères** (Hamza Vendeur — 2 ouvertes, 2 clôturées), une photo distincte par annonce.

---

## Captures d'écran

Captures générées avec **Playwright** (Chrome headless, viewport **1366 × 768**, PNG pleine page).
Le script `tools/capture-screenshots.mjs` rejoue tous les parcours ; les comptes de démonstration
viennent du README et l'administrateur est lu dans `APP_ADMIN_EMAIL` / `APP_ADMIN_PASSWORD`
(aucun identifiant n'est écrit dans le script).

```powershell
$env:APP_ADMIN_EMAIL    = "votre-admin@exemple.tn"
$env:APP_ADMIN_PASSWORD = "votre-mot-de-passe"
node tools/capture-screenshots.mjs      # écrit dans docs/screenshots/
```

### Visiteur (non connecté)

| Capture | Page |
|---|---|
| <img src="docs/screenshots/01-accueil.png" width="420"> | **01 — Accueil public.** Page d'accueil d'un visiteur : message de bienvenue, boutons *Voir les annonces* / *Créer un compte* / *Se connecter* et quatre atouts. |
| <img src="docs/screenshots/02-annonces.png" width="420"> | **02 — Liste des annonces.** Les 12 annonces (9 par page) avec le formulaire de recherche à six filtres et les cases de comparaison. |
| <img src="docs/screenshots/03-annonces-filtres.png" width="420"> | **03 — Liste filtrée.** Filtres `make=Renault` et `maxPrice=80000` : il ne reste que les deux Clio 5, et les valeurs saisies restent affichées. |
| <img src="docs/screenshots/04-annonces-page-2.png" width="420"> | **04 — Pagination.** Page 2 sur 2 : les trois dernières annonces, avec le bouton *Précédent*. |
| <img src="docs/screenshots/05-detail-prix-fixe.png" width="420"> | **05 — Détail d'une annonce à prix fixe.** Renault Clio 5 Neuve à 74 900 DT : badge *Prix fixe*, aucun bloc d'enchère. |
| <img src="docs/screenshots/06-detail-enchere.png" width="420"> | **06 — Détail d'une enchère clôturée.** Dacia Sandero : meilleure offre, **vainqueur** mis en avant et historique des trois offres avec leurs dates. |
| <img src="docs/screenshots/07-detail-enchere-ouverte.png" width="420"> | **07 — Détail d'une enchère ouverte (utilisateur connecté).** Golf 7 : statut *OUVERTE*, champ *Votre offre* et bouton *Placer mon offre*, favoris, signalement et formulaire de message. |
| <img src="docs/screenshots/08-login.png" width="420"> | **08 — Connexion.** Formulaire email + mot de passe. |
| <img src="docs/screenshots/09-register.png" width="420"> | **09 — Inscription.** Création d'un compte (nom, email, mot de passe et confirmation). |

### Utilisateur connecté

| Capture | Page |
|---|---|
| <img src="docs/screenshots/10-dashboard.png" width="420"> | **10 — Tableau de bord.** Ahmed Encherisseur : cinq compteurs (0 annonce, 4 offres, 0 gagnée, 2 favoris, 0 non lu), les quatre listes courtes et les actions rapides. |
| <img src="docs/screenshots/11-annonce-new.png" width="420"> | **11 — Publier une annonce.** Formulaire complet : titre, description, marque, modèle, année, kilométrage, prix, type et photos. |
| <img src="docs/screenshots/12-annonce-edit.png" width="420"> | **12 — Modifier une annonce.** Golf 7 de Hamza, formulaire prérempli et photos actuelles conservées. |
| <img src="docs/screenshots/13-mes-favoris.png" width="420"> | **13 — Mes favoris.** Les deux annonces mises en favori par Ahmed, avec photo, prix et bouton *Retirer des favoris*. |
| <img src="docs/screenshots/14-mes-messages.png" width="420"> | **14 — Mes messages.** Boîte de réception d'AutoPlus Tunisie : un message **non lu** en évidence, puis la section *Messages envoyés*. |
| <img src="docs/screenshots/15-profil.png" width="420"> | **15 — Mon profil.** Nom et email du compte, avec le bloc de changement de mot de passe (champs vides = inchangé). |
| <img src="docs/screenshots/16-comparer.png" width="420"> | **16 — Comparaison.** Trois annonces côte à côte (Clio 5, i10, Picanto) : photo, marque, modèle, année, kilométrage, prix, type et vendeur. |

### Administration

| Capture | Page |
|---|---|
| <img src="docs/screenshots/17-admin-dashboard.png" width="420"> | **17 — Tableau de bord administrateur.** Compteurs globaux (4 utilisateurs, 12 annonces, 2 enchères ouvertes, 4 offres, 1 annonce signalée) et accès aux deux pages de gestion. |
| <img src="docs/screenshots/18-admin-annonces.png" width="420"> | **18 — Modération des annonces.** L'annonce **signalée** (Clio 5 rouge) apparaît en tête avec son badge, chaque ligne proposant la suppression. |
| <img src="docs/screenshots/19-admin-utilisateurs.png" width="420"> | **19 — Gestion des comptes.** Les quatre comptes avec leur rôle et leur état, et le bouton de désactivation. |

### Documentation de l'API

| Capture | Page |
|---|---|
| <img src="docs/screenshots/20-swagger.png" width="420"> | **20 — Swagger UI.** `/swagger-ui.html` : le tag *Enchères*, l'opération `GET /api/annonces/{id}/meilleure-offre` et le schéma `MeilleureOffreView`. |

---

## Tests

```powershell
$env:SPRING_DATASOURCE_PASSWORD = "votre_mot_de_passe"
.\mvnw.cmd test
```

> PostgreSQL doit être démarré : `DemoApplicationTests` charge tout le contexte Spring (donc la base).

| Classe | Contenu |
|--------|---------|
| `AnnonceControllerTest` | Tâche 1 du TP5 — **10 tests** `@WebMvcTest` + `MockMvc`, service simulé : statut HTTP, nom de vue, redirection, modèle |
| `AnnonceServiceTest` | Tâche 2 du TP5 — **16 tests** JUnit 5 + Mockito (`@Mock` / `@InjectMocks`), aucune base de données |
| `DemoApplicationTests` | **1 test** de démarrage du contexte |

Résultat attendu : **27 tests, 0 échec**, `BUILD SUCCESS`.

> Note : sur Spring Boot 4, `@MockBean` n'existe plus — c'est `@MockitoBean` qui le remplace
> (`org.springframework.test.context.bean.override.mockito.MockitoBean`).

---

## Modules

| Module | État |
|--------|------|
| M1 Comptes & auth | Implémenté |
| M2 Annonces | Implémenté |
| M3 Enchères | Implémenté |
| M4 Interactions | Implémenté |
| M5 Administration | Implémenté |

## M5 — Administration (détails)

### Les pages

| Page | Rôle |
|------|------|
| `/admin` | Tableau de bord : utilisateurs, annonces, enchères ouvertes, offres placées, annonces signalées |
| `/admin/annonces` | Modération : les annonces **signalées en premier**, avec le bouton de suppression |
| `/admin/utilisateurs` | Activation / désactivation des comptes |

- **Signaler une annonce** : sur la page d'une annonce, un utilisateur connecté qui n'en est pas le
  vendeur peut la signaler. L'annonce passe alors en tête de la liste d'administration.
- **Suppression par l'admin** : les offres, messages et favoris de l'annonce sont supprimés **avant**
  l'annonce, sinon la clé étrangère provoquerait une erreur 500.
- **Désactiver un compte** : `User.active` est déjà relié à `UserDetails.isEnabled()`, donc Spring
  Security refuse la connexion d'un compte désactivé. Un administrateur ne peut pas désactiver
  son propre compte.

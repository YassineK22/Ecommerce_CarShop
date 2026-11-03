# DÉMONSTRATION — AutoHub / Module M3 (Enchères)

## 0. Avant de commencer (si la machine vient de s'allumer)

1. Ouvrir PowerShell, démarrer la base de données :
   ```powershell
   docker start autohub-db
   ```
2. Lancer l'application (dans le dossier `projet\demo`) :
   ```powershell
   mvn -o spring-boot:run
   ```
3. Ouvrir http://localhost:8080

## 1. Comptes de démonstration

| Rôle | Email | Mot de passe |
|---|---|---|
| Vendeur (owner) | `hamza@autohub.tn` | `Passer1234` |
| Acheteur (encherisseur) | `ahmed@autohub.tn` | `Passer1234` |

Deux annonces déjà créées :
- **Annonce 1 — "Peugeot 208 Essai Enchère Auto"** : enchère CLÔTURÉE, gagnant = Ahmed (parfaite pour montrer le résultat final).
- **Annonce 2 — "Dacia Sandero Stepway"** : enchère OUVERTE jusqu'au 29/10 (parfaite pour montrer les enchères en direct).

## 2. Scénario à montrer (dans cet ordre)

### A. Résultat d'une enchère clôturée (30 s)
1. Connexion avec `ahmed@autohub.tn`.
2. Menu **Annonces → Peugeot 208**.
3. Montrer : statut **CLOTUREE**, bandeau **🏆 Vainqueur de l'enchère : Ahmed Encherisseur**,
   meilleure offre **12.000,00 DT**, et le tableau **Historique des offres** (encherisseur, montant, date).
4. À dire : « L'enchère s'est fermée automatiquement à sa date limite et le système
   a désigné le gagnant automatiquement — c'est le job planifié M3. »

### B. Enchère en cours — placer une offre (1 min)
1. Ouvrir **Dacia Sandero Stepway** (annonce 2, OUVERTE).
2. Montrer : « Meilleure offre actuelle : 9.500,00 DT (offre de Ahmed) »,
   le champ d'offre bloqué à min 9.500,01, l'historique en dessous.
3. **Bid une offre valide** : taper `9600` → « Votre offre a été enregistrée »,
   la page se rafraîchit, la meilleure offre devient 9.600,00 DT, une nouvelle
   ligne apparaît dans l'historique.

### C. Les règles métier (1 min — c'est ce qui rapporte des points)
Montrer dans l'ordre :
1. **Offre trop basse** : retaper `9500` → message « Cette valeur doit être
   supérieure ou égale à 9.500,01 » (validation cliente) ; dire que la règle est
   aussi vérifiée **côté serveur** dans `OffreService.placerOffre()`.
2. **Déconnexion → visiter l'annonce** : le visiteur ne voit pas le formulaire,
   juste « Connectez-vous pour enchérir » (Spring Security).
3. **Connexion Hamza (vendeur) → ouvrir sa propre annonce** : pas de formulaire,
   message « C'est votre annonce — vous ne pouvez pas enchérir sur vos propres annonces ».
4. À dire : toutes ces règles sont codées dans **`OffreService.placerOffre()`** :
   type ENCHERE obligatoire, statut OUVERTE, date non dépassée, pas d'auto-enchère,
   montant strictement supérieur à la meilleure offre.

### D. L'endpoint REST (30 s)
Ouvrir (avec Ahmed connecté ou même sans connexion) :
```
http://localhost:8080/api/annonces/2/meilleure-offre
```
→ JSON : `montant`, `encherisseur`, `statut`, `finEnchere`.
À dire : « c'est prévu pour que le front rafraîchisse la meilleure offre sans
recharger la page (ex: fetch/JS en option) ».

### E. La clôture automatique — 2 options
- **Option simple** : montrer la console : ligne
  `INFO ... Enchère clôturée : annonce 1 - Peugeot 208 ...` avec le thread
  `[scheduling-1]` → preuve que `@Scheduled` (60 s) a tourné sans intervention.
- **Option live** (si assez de temps) : créer une annonce enchère avec
  date de fin à **maintenant + 3 min**, bid avec Ahmed, patienter → le statut
  passe à CLOTUREE tout seul et le gagnant apparaît.

### F. La base de données (30 s)
```powershell
docker exec -it autohub-db psql -U postgres -d autohub
\dt                  -- liste des tables
```
Puis :
```sql
SELECT id, title, type, statut_enchere, winner_id FROM annonces;
SELECT * FROM offres;
```
À dire : « notre offre bid d'un instant → a été stockée dans la table offres :
une ligne = un enregistrement ; Hibernate a généré le SQL depuis OffreRepository. »

## 3. Les fichiers M3 à citer (si le prof demande "où est le code ?")

| Fichier | Rôle |
|---|---|
| `entity/Offre.java` | entité offre : montant, date, bidder, annonce |
| `repository/OffreRepository.java` | requêtes (liste des offres triée par montant) |
| `service/OffreService.java` | TOUTES les règles métier d'enchère |
| `service/EnchereScheduler.java` | contrauto : clôture toutes les 60 s + gagnant |
| `controller/OffreController.java` | POST /annonces/{id}/offres (formulaire Thymeleaf) |
| `controller/EnchereRestController.java` | GET /api/annonces/{id}/meilleure-offre (JSON) |
| `templates/annonces/detail.html` | section Enchères : meilleure offre, formulaire, historique |
| `DemoApplication.java` | contient `@EnableScheduling` (nécessaire au scheduler) |

## 4. Questions probables du prof + réponses courtes

- **Où est stockée la meilleure offre ?** → Nulle part en doublon : elle est
  *calculée* = première ligne de `offres` triée par montant DESC ; si aucune
  offre, ça retombe sur le prix de départ (`annonce.price`). Une seule source de vérité.
- **Comment l'offreur est proposé ?** → entité `Offre` avec FK `annonce_id`,
  `bidder_id`… le HTML ne vient pas de la BDD : JPA → objets Java → Model → Thymeleaf.
- **Pourquoi @Scheduled ?** → sans elle Spring ignore `@Scheduled` → rien ne clôture.
- **Sécurité ?** → Spring Security : /annonces GET public, POST authentifié,
  REST d'offre lisible publiquement (comme la fiche), CSRF activé par Spring.
- **Et-owner ne peut pas bidder ?** → testé côté UI (formulaire caché) ET côté
  serveur (`OffreService` refuse) — on a testé avec un POST forcé : refusé aussi.

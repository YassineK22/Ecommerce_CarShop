# AutoHub

Marketplace Spring Boot + Thymeleaf (vente et enchères de voitures d'occasion).

## PostgreSQL (obligatoire)

L'application **n'utilise pas** H2 ni une autre base en mémoire. Les données doivent survivre aux redémarrages.

### 1. Installer et démarrer PostgreSQL

- Télécharger PostgreSQL : https://www.postgresql.org/download/windows/
- Démarrer le service **postgresql-x64-…** (Services Windows ou pgAdmin).

### 2. Créer la base `autohub`

Avec **pgAdmin** ou **psql** :

```sql
CREATE DATABASE autohub;
```

### 3. Configurer les identifiants (sans les committer)

**Option A — variables d'environnement (recommandé)**

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/autohub"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "votre_mot_de_passe"
```

**Option B — fichier local**

Copier `src/main/resources/application-local.properties.example` vers `application-local.properties` (gitignored), puis :

```properties
spring.config.import=optional:classpath:application-local.properties
```

(dans `application.properties` ou en profil IDE)

### 4. Lancer l'application

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.20"
.\mvnw.cmd spring-boot:run
```

Hibernate crée/met à jour les tables avec `spring.jpa.hibernate.ddl-auto=update` (sans effacer les données existantes).

### Erreur de connexion

Si PostgreSQL n'est pas démarré ou la base n'existe pas, le démarrage échoue avec une erreur du type *Connection refused* ou *database "autohub" does not exist*. Corrigez le service, la base, l'URL ou le mot de passe — il n'y a **pas** de repli H2.

## Comptes de démonstration

Les trois comptes sont créés par le formulaire d'inscription de l'application (mot de passe hashé en BCrypt).
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

### Contenu de la base de démonstration

12 annonces : **8 prix fixe** (AutoPlus Tunisie — 3 neuves 2026 / 0 km et 5 occasions) et
**4 enchères** (Hamza Vendeur — 2 ouvertes, 2 clôturées), une photo distincte par annonce.

## Modules

| Module | État |
|--------|------|
| M1 Comptes & auth | Implémenté |
| M2 Annonces | Implémenté |
| M3 Enchères | Implémenté |
| M4 Interactions | Implémenté |
| M5 Admin | Entités + dépôt (`User.active`, `Signalement`) |

# Maketo API — référence pour le front

Base : `http://localhost:8081/api` (dev). Tout est en JSON, sauf les uploads (multipart).

## Conventions

- **Auth** : `Authorization: Bearer <accessToken>` (valable 15 min).
  Quand une requête renvoie **401**, appeler `POST /auth/rafraichir` (avec `credentials: "include"` pour envoyer le cookie), puis rejouer la requête. Si le rafraîchissement renvoie lui aussi 401 → écran de connexion.
- **Cookie** `maketo_refresh` : httpOnly, posé par `/auth/connexion` et `/auth/rafraichir`. Le front ne le lit jamais.
- **Erreurs** (format RFC 9457) :
  ```json
  { "status": 400, "title": "Données invalides", "detail": "Certains champs sont incorrects",
    "erreurs": { "telephone": "Numéro camerounais invalide (ex : 6XXXXXXXX)" } }
  ```
  Afficher `detail`, et `erreurs[champ]` sous chaque champ du formulaire.
- **Pagination** : `?page=0&size=20` (50 maximum). Réponse : `{ contenu: [], page, taille, totalElements, totalPages }`.
- **Dates** : ISO 8601 UTC (`2026-11-11T02:48:13Z`). À afficher dans le fuseau Africa/Douala.
- **Téléphones** : acceptés en `6XXXXXXXX`, `6XX XX XX XX` ou `+237…`. Toujours renvoyés en `6XXXXXXXX`.

Légende : 🌐 public · 🔒 connecté · 👑 admin

---

## Authentification

| Méthode | Route | Accès | Corps → Réponse |
|---|---|---|---|
| POST | `/auth/inscription` | 🌐 | `{nom, prenom, telephone, whatsapp?, matricule, filiere?, niveau?, motDePasse}` → 201 `Utilisateur` (statut `EN_ATTENTE`) |
| POST | `/auth/connexion` | 🌐 | `{telephone, motDePasse}` → `{accessToken, tokenType, expireDansSecondes, utilisateur}` + cookie. 403 si le compte est en attente ou bloqué, 429 après 5 échecs |
| POST | `/auth/rafraichir` | 🌐 (cookie) | → même réponse que la connexion |
| POST | `/auth/deconnexion` | 🌐 (cookie) | → 204 |

## Mon compte

| Méthode | Route | Accès | Corps → Réponse |
|---|---|---|---|
| GET | `/utilisateurs/moi` | 🔒 | → `Utilisateur` |
| PUT | `/utilisateurs/moi` | 🔒 | `{nom, prenom, whatsapp?, filiere?, niveau?}` → `Utilisateur` |
| PATCH | `/utilisateurs/moi/mot-de-passe` | 🔒 | `{ancien, nouveau}` → 204. **Toutes les sessions sont coupées : renvoyer vers la connexion** |
| GET | `/utilisateurs/moi/annonces` | 🔒 | → page de `MonAnnonce` |
| GET | `/utilisateurs/moi/favoris` | 🔒 | → page de `{annonce: AnnonceResume, disponible, dateAjout}` |
| GET | `/utilisateurs/moi/favoris/ids` | 🔒 | → `[12, 15]` (pour afficher les cœurs pleins) |
| GET | `/utilisateurs/moi/portefeuille` | 🔒 | → `{solde, pro, proExpireLe, paiementsActifs}` |
| GET | `/utilisateurs/moi/transactions` | 🔒 | → page de `{id, type, montant, soldeApres, libelle, annonceId, date}` |

## Catégories et annonces

| Méthode | Route | Accès | Corps → Réponse |
|---|---|---|---|
| GET | `/categories` | 🌐 | → `[{id, nom, slug, icone}]` |
| GET | `/annonces` | 🌐 | `?type=ARTICLE\|MESSAGE&categorie=slug&q=texte&prixMin&prixMax&etat=NEUF\|OCCASION&tri=RECENT\|PRIX_CROISSANT\|PRIX_DECROISSANT&page&taille` → page de `AnnonceResume` |
| GET | `/annonces/a-la-une` | 🌐 | → `[AnnonceResume]` (10 au maximum) |
| GET | `/annonces/{id}` | 🌐 | → `AnnonceDetail` (`vendeur.whatsapp` vaut `null` si le visiteur n'est pas connecté) |
| POST | `/annonces` | 🔒 | `{type?, categorieId, titre?, description?, prix?, prixADebattre, etat?, lieu?}` → 201 `AnnonceDetail`. 400 si le quota est atteint (voir « Petites annonces » ci-dessous) |
| PUT | `/annonces/{id}` | 🔒 propriétaire | même corps (le `type` ne change pas après création) → `AnnonceDetail` |
| PATCH | `/annonces/{id}/vendue` | 🔒 propriétaire | → `AnnonceDetail` |
| PATCH | `/annonces/{id}/renouveler` | 🔒 propriétaire | → `AnnonceDetail` (seulement quand `MonAnnonce.renouvelable` vaut true) |
| DELETE | `/annonces/{id}` | 🔒 propriétaire / 👑 | → 204 |
| POST | `/annonces/{id}/contact` | 🔒 | → `{lienWhatsapp, telephone}` : ouvrir `lienWhatsapp` |
| POST | `/annonces/{id}/photos` | 🔒 propriétaire | multipart `fichier` (JPG ou PNG, 5 Mo maximum) → 201 `AnnonceDetail` |
| DELETE | `/annonces/{id}/photos/{photoId}` | 🔒 propriétaire | → `AnnonceDetail` |
| POST / DELETE | `/annonces/{id}/favori` | 🔒 | → 204 (idempotent) |
| POST | `/annonces/{id}/signalements` | 🔒 | `{motif: ARNAQUE\|INTERDIT\|DOUBLON\|INAPPROPRIE\|AUTRE, commentaire?}` → 201. 409 si déjà signalé |
| GET | `/vendeurs/{id}` | 🌐 | → `{vendeur, membreDepuis, nbAnnoncesEnLigne}` |
| GET | `/vendeurs/{id}/annonces` | 🌐 | → page de `AnnonceResume` |

**Photos** : compresser côté front avant l'envoi (canvas, 1280 px, JPEG 0,8). Ça économise le forfait data des étudiants. Le serveur re-traite de toute façon chaque image.

### Petites annonces (`type`)

- `ARTICLE` (par défaut) : `titre` (3 à 100 caractères), `etat`, et `prix` ou `prixADebattre=true` sont obligatoires.
- `MESSAGE` : simple texte. Seule la `description` est obligatoire (10 caractères minimum). `prix`, `etat` et `prixADebattre` sont ignorés (mis à null / false). Si `titre` est vide, il est tiré de la première ligne du message (80 caractères au maximum). Les photos sont facultatives. Catégorie conseillée : `petites-annonces`.
- `AnnonceResume` contient `type` et `extrait` (160 premiers caractères du message, `null` pour un article). `AnnonceDetail` et `MonAnnonce` contiennent `type`.
- Les messages suivent les mêmes règles que les articles : quota, expiration, boost, signalements.

## Monétisation

Tant que `paiementsActifs` vaut `false` (période de lancement), ces routes renvoient 400 : cacher les boutons correspondants.

| Méthode | Route | Accès | Corps → Réponse |
|---|---|---|---|
| GET | `/offres` | 🌐 | → `[{code, libelle, type: BOOST\|ABONNEMENT, prix, dureeHeures}]` |
| GET | `/paiements/options` | 🔒 | → `{paiementsActifs, montants: [500, 1000, ...], operateurs: [{cle: "MOMO", nom}]}` |
| POST | `/paiements/recharges` | 🔒 | `{montant, operateur, telephone}` → 201 `{reference, montant, operateur, statut: EN_ATTENTE, message}` |
| GET | `/paiements/recharges/{reference}` | 🔒 | → même objet. **Interroger toutes les 4 s** jusqu'à `REUSSI` ou `ECHOUE` (2 minutes maximum) |
| POST | `/annonces/{id}/boost` | 🔒 propriétaire | `{offreCode?}` (défaut `BOOST_24H`) → `{soldeRestant, jusqua}`. 400 si le solde est insuffisant |
| POST | `/abonnements/pro` | 🔒 | `{offreCode?}` (défaut `PRO_MENSUEL`) → `{soldeRestant, jusqua}` |

Écran de recharge : afficher le message « Valide le paiement sur ton téléphone » pendant l'attente.
En développement, le fournisseur « simulation » valide le paiement après 10 s. Un numéro qui finit par `0000` simule un échec.

## Publicités

| Méthode | Route | Accès | Corps → Réponse |
|---|---|---|---|
| GET | `/publicites` | 🌐 | → `[{id, annonceur, titre, imageUrl, lien}]` (bannières en cours) |
| POST | `/publicites/{id}/clic` | 🌐 | → 204 (à appeler avant d'ouvrir `lien`) |

## Administration 👑

| Méthode | Route | Corps → Réponse |
|---|---|---|
| GET | `/admin/statistiques` | → tableau de bord (inscrits, annonces, signalements, montant encaissé ce mois…) |
| GET | `/admin/utilisateurs?statut=EN_ATTENTE\|ACTIF\|BLOQUE` | → page de `Utilisateur` |
| PATCH | `/admin/utilisateurs/{id}/activer` · `/bloquer` | → `Utilisateur` |
| POST | `/admin/utilisateurs/{id}/credits` | `{montant, type: BONUS\|REMBOURSEMENT}` → portefeuille |
| GET | `/admin/moderation/signalements?statut=OUVERT\|TRAITE` | → page de signalements |
| POST | `/admin/moderation/annonces/{id}/retablir` · `/retirer` | → 204 |
| GET | `/admin/paiements?statut=` | → page de paiements |
| GET | `/admin/parametres` | → `[{cle, valeur, type, description}]` |
| PUT | `/admin/parametres/{cle}` | `{valeur}` → paramètre (ex. `paiements_actifs` = `true` le jour J) |
| GET / POST | `/admin/categories` | `{nom, slug, icone?, ordreAffichage}` |
| PUT | `/admin/categories/{id}` · PATCH `/{id}/activer` · `/desactiver` | |
| GET / POST | `/admin/publicites` | POST en multipart : `image` + `annonceur, titre?, lien?, dateDebut, dateFin` (format AAAA-MM-JJ) |
| PATCH | `/admin/publicites/{id}/activer` · `/desactiver` | |

---

## Objets principaux

**Utilisateur** : `id, nom, prenom, telephone, whatsapp, matricule, filiere, niveau, role (ETUDIANT|ADMIN), statut, pro, proExpireLe, soldeCredits, dateInscription`

**AnnonceResume** (carte) : `id, titre, prix, prixADebattre, etat, lieu, categorieSlug, photoPrincipale, boostee, vendeurPro, dateCreation`

**AnnonceDetail** : `id, titre, description, prix, prixADebattre, etat, lieu, categorie{}, photos[{id, url}], vendeur{id, nomAffiche, filiere, pro, whatsapp}, statut, boostee, nbVues, dateCreation, dateExpiration`

**MonAnnonce** : `id, titre, prix, prixADebattre, photoPrincipale, statut (ACTIVE|EXPIREE|VENDUE|SUSPENDUE), formule, visible, boostee, renouvelable, nbVues, nbContacts, nbRenouvellements, dateCreation, dateExpiration`

## Mise en production (variables d'environnement)

| Variable | Exemple |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | base PostgreSQL |
| `JWT_SECRET` | 64 caractères aléatoires minimum |
| `CORS_ORIGINS` | `https://maketo.vercel.app` |
| `COOKIE_SECURE` / `COOKIE_SAME_SITE` | `true` / `None` (si le front et l'API sont sur des domaines différents) |
| `ADMIN_TELEPHONE`, `ADMIN_MOT_DE_PASSE` | compte admin initial |
| `STOCKAGE_TYPE` + `CLOUDINARY_*` | `cloudinary` |
| `PAIEMENT_FOURNISSEUR` | `ctpay` |
| `CTPAY_TOKEN` | token marchand CT Pay |
| `PAIEMENT_CALLBACK_URL` | `https://<api>/api/paiements/callback/ctpay` |

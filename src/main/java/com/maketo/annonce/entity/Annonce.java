package com.maketo.annonce.entity;

import com.maketo.categorie.entity.Categorie;
import com.maketo.common.entity.AuditableEntity;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.utilisateur.entity.Utilisateur;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Racine de l'agrégat Annonce : contient les règles métier de son cycle de vie.
 */
@Entity
@Table(name = "annonce")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Annonce extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendeur_id", nullable = false)
    private Utilisateur vendeur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeAnnonce type = TypeAnnonce.ARTICLE;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(length = 2000)
    private String description;

    @Column
    private Integer prix;

    @Column(name = "prix_a_debattre", nullable = false)
    private Boolean prixADebattre = Boolean.FALSE;

    /** Null pour une petite annonce (MESSAGE). */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EtatArticle etat;

    @Column(length = 100)
    private String lieu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Formule formule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAnnonce statut = StatutAnnonce.ACTIVE;

    @Column(name = "boost_jusqu_a")
    private Instant boostJusquA;

    @Column(name = "nb_vues", nullable = false)
    private Integer nbVues = 0;

    @Column(name = "nb_contacts", nullable = false)
    private Integer nbContacts = 0;

    @Column(name = "nb_renouvellements", nullable = false)
    private Integer nbRenouvellements = 0;

    @Column(name = "date_dernier_renouvellement")
    private Instant dateDernierRenouvellement;

    @Column(name = "date_expiration", nullable = false)
    private Instant dateExpiration;

    @OneToMany(mappedBy = "annonce", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<PhotoAnnonce> photos = new ArrayList<>();

    // ------------------------------------------------------------------
    // Création / modification
    // ------------------------------------------------------------------

    public static Annonce publier(Utilisateur vendeur, Categorie categorie, TypeAnnonce type, String titre,
                                  String description, Integer prix, boolean prixADebattre, EtatArticle etat,
                                  String lieu, Formule formule, Instant dateExpiration) {
        Annonce a = new Annonce();
        a.type = type == null ? TypeAnnonce.ARTICLE : type;
        a.vendeur = Objects.requireNonNull(vendeur);
        a.formule = Objects.requireNonNull(formule);
        a.dateExpiration = Objects.requireNonNull(dateExpiration);
        a.appliquerContenu(categorie, titre, description, prix, prixADebattre, etat, lieu);
        return a;
    }

    public void modifier(Categorie categorie, String titre, String description, Integer prix,
                         boolean prixADebattre, EtatArticle etat, String lieu) {
        verifierModifiable();
        appliquerContenu(categorie, titre, description, prix, prixADebattre, etat, lieu);
    }

    /** Le type ne change jamais après la création : seules les règles de son type s'appliquent. */
    private void appliquerContenu(Categorie categorie, String titre, String description, Integer prix,
                                  boolean prixADebattre, EtatArticle etat, String lieu) {
        this.categorie = Objects.requireNonNull(categorie);
        this.lieu = lieu;

        if (type == TypeAnnonce.MESSAGE) {
            if (description == null || description.strip().length() < 10) {
                throw new MetierException("Écris ton message (10 caractères minimum)");
            }
            this.description = description.strip();
            this.titre = (titre == null || titre.isBlank()) ? titreDepuis(this.description) : titre.strip();
            this.prix = null;
            this.prixADebattre = Boolean.FALSE;
            this.etat = null;
            return;
        }

        if (titre == null || titre.strip().length() < 3) {
            throw new MetierException("Le titre doit faire au moins 3 caractères");
        }
        if (etat == null) {
            throw new MetierException("Indique si l'article est neuf ou d'occasion");
        }
        if (!prixADebattre && prix == null) {
            throw new MetierException("Indique un prix ou coche « prix à débattre »");
        }
        if (prix != null && prix < 0) {
            throw new MetierException("Le prix ne peut pas être négatif");
        }
        this.titre = titre.strip();
        this.description = description;
        this.prix = prix;
        this.prixADebattre = prixADebattre;
        this.etat = etat;
    }

    /** Titre d'une petite annonce : sa première ligne, raccourcie proprement à 80 caractères. */
    static String titreDepuis(String message) {
        String ligne = message.strip().lines().findFirst().orElse("").replaceAll("\\s+", " ");
        if (ligne.length() <= 80) {
            return ligne;
        }
        int coupure = ligne.lastIndexOf(' ', 79);
        return ligne.substring(0, coupure > 40 ? coupure : 79) + "…";
    }

    public boolean estMessage() {
        return type == TypeAnnonce.MESSAGE;
    }

    // ------------------------------------------------------------------
    // Photos
    // ------------------------------------------------------------------

    public PhotoAnnonce ajouterPhoto(String url, String publicId, int nbMaxPhotos) {
        verifierModifiable();
        if (photos.size() >= nbMaxPhotos) {
            throw new MetierException("Maximum " + nbMaxPhotos + " photos par annonce");
        }
        PhotoAnnonce photo = new PhotoAnnonce(this, url, publicId, (short) photos.size());
        photos.add(photo);
        return photo;
    }

    /** Retire une photo et recompacte l'ordre (0, 1, 2...) : la première restante devient la photo principale. */
    public PhotoAnnonce retirerPhoto(Long photoId) {
        verifierModifiable();
        PhotoAnnonce photo = photos.stream()
                .filter(p -> p.getId().equals(photoId))
                .findFirst()
                .orElseThrow(() -> new RessourceIntrouvableException("Photo introuvable"));
        photos.remove(photo);
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).changerOrdre((short) i);
        }
        return photo;
    }

    public List<PhotoAnnonce> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    // ------------------------------------------------------------------
    // Cycle de vie
    // ------------------------------------------------------------------

    public void marquerVendue() {
        verifierModifiable();
        this.statut = StatutAnnonce.VENDUE;
    }

    public void supprimer() {
        this.statut = StatutAnnonce.SUPPRIMEE;
    }

    public void expirer() {
        if (statut == StatutAnnonce.ACTIVE) {
            this.statut = StatutAnnonce.EXPIREE;
        }
    }

    public void renouveler(Formule nouvelleFormule, Instant nouvelleExpiration, Instant maintenant) {
        verifierModifiable();
        this.formule = Objects.requireNonNull(nouvelleFormule);
        this.statut = StatutAnnonce.ACTIVE;
        this.dateExpiration = nouvelleExpiration;
        this.dateDernierRenouvellement = maintenant;
        this.nbRenouvellements++;
    }

    /** Masquage automatique (trop de signalements). */
    public void suspendre() {
        if (statut == StatutAnnonce.ACTIVE) {
            this.statut = StatutAnnonce.SUSPENDUE;
        }
    }

    /** Décision de l'admin après vérification : l'annonce redevient visible. */
    public void retablir() {
        if (statut != StatutAnnonce.SUSPENDUE) {
            throw new MetierException("Cette annonce n'est pas suspendue");
        }
        this.statut = StatutAnnonce.ACTIVE;
    }

    public void booster(Instant jusqua) {
        if (statut != StatutAnnonce.ACTIVE) {
            throw new MetierException("Seule une annonce active peut être boostée");
        }
        this.boostJusquA = jusqua;
    }

    // ------------------------------------------------------------------
    // Requêtes métier
    // ------------------------------------------------------------------

    public boolean estVisible(Instant maintenant) {
        return statut == StatutAnnonce.ACTIVE && dateExpiration.isAfter(maintenant);
    }

    public boolean estBoostee(Instant maintenant) {
        return boostJusquA != null && boostJusquA.isAfter(maintenant);
    }

    public boolean appartientA(Long utilisateurId) {
        return vendeur.getId().equals(utilisateurId);
    }

    private void verifierModifiable() {
        if (statut == StatutAnnonce.VENDUE || statut == StatutAnnonce.SUPPRIMEE) {
            throw new MetierException("Cette annonce n'est plus modifiable");
        }
        if (statut == StatutAnnonce.SUSPENDUE) {
            throw new MetierException("Cette annonce est suspendue en attente de vérification par l'administrateur");
        }
    }
}

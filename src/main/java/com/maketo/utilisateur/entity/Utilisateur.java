package com.maketo.utilisateur.entity;

import com.maketo.common.entity.AuditableEntity;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.SoldeInsuffisantException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "utilisateur")
@AttributeOverride(name = "dateCreation",
        column = @Column(name = "date_inscription", nullable = false, updatable = false))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Utilisateur extends AuditableEntity {

    @Setter
    @Column(nullable = false, length = 80)
    private String nom;

    @Setter
    @Column(nullable = false, length = 80)
    private String prenom;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Setter
    @Column(length = 20)
    private String whatsapp;

    @Column(nullable = false, unique = true, length = 30)
    private String matricule;

    @Setter
    @Column(length = 100)
    private String filiere;

    @Setter
    @Column(length = 20)
    private String niveau;

    @Column(name = "mot_de_passe", nullable = false, length = 100)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.ETUDIANT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutUtilisateur statut = StatutUtilisateur.EN_ATTENTE;

    @Column(name = "pro_expire_le")
    private Instant proExpireLe;

    @Column(name = "solde_credits", nullable = false)
    private Integer soldeCredits = 0;

    /** Verrou optimiste : empêche deux débits simultanés du même solde. */
    @Version
    @Column(nullable = false)
    private Long version;

    // ------------------------------------------------------------------
    // Création
    // ------------------------------------------------------------------

    public static Utilisateur inscrire(String nom, String prenom, String telephone, String whatsapp,
                                       String matricule, String filiere, String niveau,
                                       String motDePasseHache) {
        Utilisateur u = new Utilisateur();
        u.nom = nom;
        u.prenom = prenom;
        u.telephone = telephone;
        u.whatsapp = (whatsapp == null || whatsapp.isBlank()) ? telephone : whatsapp;
        u.matricule = matricule;
        u.filiere = filiere;
        u.niveau = niveau;
        u.motDePasse = motDePasseHache;
        return u;
    }

    // ------------------------------------------------------------------
    // Compte
    // ------------------------------------------------------------------

    public boolean estActif() {
        return statut == StatutUtilisateur.ACTIF;
    }

    public boolean estAdmin() {
        return role == Role.ADMIN;
    }

    public void activer() {
        this.statut = StatutUtilisateur.ACTIF;
    }

    public void bloquer() {
        this.statut = StatutUtilisateur.BLOQUE;
    }

    public void promouvoirAdmin() {
        this.role = Role.ADMIN;
        this.statut = StatutUtilisateur.ACTIF;
    }

    public void changerMotDePasse(String nouveauMotDePasseHache) {
        this.motDePasse = nouveauMotDePasseHache;
    }

    // ------------------------------------------------------------------
    // Abonnement Pro (pas de booléen stocké : Pro = date de fin dans le futur)
    // ------------------------------------------------------------------

    public boolean estPro() {
        return estPro(Instant.now());
    }

    public boolean estPro(Instant maintenant) {
        return proExpireLe != null && proExpireLe.isAfter(maintenant);
    }

    /** Prolonge l'abonnement : s'ajoute à la période en cours si elle n'est pas finie. */
    public void prolongerPro(Duration duree, Instant maintenant) {
        Instant base = estPro(maintenant) ? proExpireLe : maintenant;
        this.proExpireLe = base.plus(duree);
    }

    // ------------------------------------------------------------------
    // Crédits
    // ------------------------------------------------------------------

    public void crediter(int montant) {
        if (montant <= 0) {
            throw new MetierException("Le montant à créditer doit être positif");
        }
        this.soldeCredits += montant;
    }

    public void debiter(int montant) {
        if (montant <= 0) {
            throw new MetierException("Le montant à débiter doit être positif");
        }
        if (soldeCredits < montant) {
            throw new SoldeInsuffisantException(soldeCredits, montant);
        }
        this.soldeCredits -= montant;
    }
}

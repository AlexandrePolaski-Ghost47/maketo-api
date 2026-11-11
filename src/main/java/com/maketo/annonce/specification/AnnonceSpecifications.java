package com.maketo.annonce.specification;

import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.entity.TypeAnnonce;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

/** Briques de recherche combinables (chaque filtre est indépendant et réutilisable). */
public final class AnnonceSpecifications {

    private AnnonceSpecifications() {
    }

    /** Ne dépend pas de la tâche d'expiration : une annonce dont la date est passée n'est jamais affichée. */
    public static Specification<Annonce> visibles(Instant maintenant) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("statut"), StatutAnnonce.ACTIVE),
                cb.greaterThan(root.<Instant>get("dateExpiration"), maintenant));
    }

    public static Specification<Annonce> vendeur(Long vendeurId) {
        return (root, query, cb) -> cb.equal(root.get("vendeur").get("id"), vendeurId);
    }

    public static Specification<Annonce> categorie(String slug) {
        return (root, query, cb) -> cb.equal(root.join("categorie").get("slug"), slug);
    }

    public static Specification<Annonce> texte(String recherche) {
        String motif = "%" + echapper(recherche.trim().toLowerCase()) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.<String>get("titre")), motif, '\\'),
                cb.like(cb.lower(root.<String>get("description")), motif, '\\'));
    }

    public static Specification<Annonce> prixMin(int min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<Integer>get("prix"), min);
    }

    public static Specification<Annonce> prixMax(int max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<Integer>get("prix"), max);
    }

    public static Specification<Annonce> type(TypeAnnonce type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Annonce> etat(EtatArticle etat) {
        return (root, query, cb) -> cb.equal(root.get("etat"), etat);
    }

    /** Les caractères % et _ tapés par l'utilisateur sont cherchés tels quels, pas comme jokers. */
    private static String echapper(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}

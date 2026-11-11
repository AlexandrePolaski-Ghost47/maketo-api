package com.maketo.annonce.mapper;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.dto.AnnonceResumeResponse;
import com.maketo.annonce.dto.MonAnnonceResponse;
import com.maketo.annonce.dto.PhotoResponse;
import com.maketo.annonce.dto.VendeurResponse;
import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.categorie.mapper.CategorieMapper;
import com.maketo.utilisateur.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Mapper écrit à la main : il contient des règles d'affichage (numéro masqué aux visiteurs,
 * état "boostée" calculé à l'instant T) qu'il est plus lisible d'exprimer en Java qu'en annotations.
 */
@Component
@RequiredArgsConstructor
public class AnnonceMapper {

    private final CategorieMapper categorieMapper;

    public AnnonceResumeResponse versResume(Annonce a, Instant maintenant) {
        return new AnnonceResumeResponse(
                a.getId(), a.getType(), a.getTitre(), extrait(a), a.getPrix(), a.getPrixADebattre(), a.getEtat(), a.getLieu(),
                a.getCategorie().getSlug(), photoPrincipale(a), a.estBoostee(maintenant),
                a.getVendeur().estPro(maintenant), a.getDateCreation());
    }

    public AnnonceDetailResponse versDetail(Annonce a, boolean lecteurConnecte, Instant maintenant) {
        List<PhotoResponse> photos = a.getPhotos().stream()
                .map(p -> new PhotoResponse(p.getId(), p.getUrl()))
                .toList();
        return new AnnonceDetailResponse(
                a.getId(), a.getType(), a.getTitre(), a.getDescription(), a.getPrix(), a.getPrixADebattre(), a.getEtat(),
                a.getLieu(), categorieMapper.versResponse(a.getCategorie()), photos,
                versVendeur(a.getVendeur(), lecteurConnecte, maintenant),
                a.getStatut(), a.estBoostee(maintenant), a.getNbVues(), a.getDateCreation(), a.getDateExpiration());
    }

    public MonAnnonceResponse versMonAnnonce(Annonce a, Instant maintenant) {
        boolean visible = a.estVisible(maintenant);
        boolean renouvelable = !visible
                && (a.getStatut() == StatutAnnonce.ACTIVE || a.getStatut() == StatutAnnonce.EXPIREE);
        return new MonAnnonceResponse(
                a.getId(), a.getType(), a.getTitre(), a.getPrix(), a.getPrixADebattre(), photoPrincipale(a),
                statutAffiche(a, visible),
                a.getFormule(), visible, a.estBoostee(maintenant), renouvelable,
                a.getNbVues(), a.getNbContacts(), a.getNbRenouvellements(),
                a.getDateCreation(), a.getDateExpiration());
    }

    public VendeurResponse versVendeur(Utilisateur v, boolean lecteurConnecte, Instant maintenant) {
        String nomAffiche = v.getPrenom() + " " + v.getNom().charAt(0) + ".";
        return new VendeurResponse(v.getId(), nomAffiche, v.getFiliere(), v.estPro(maintenant),
                lecteurConnecte ? v.getWhatsapp() : null);
    }

    /** Une annonce ACTIVE dont la date est passée s'affiche EXPIREE, même avant le passage de la tâche nocturne. */
    private static StatutAnnonce statutAffiche(Annonce a, boolean visible) {
        return (a.getStatut() == StatutAnnonce.ACTIVE && !visible) ? StatutAnnonce.EXPIREE : a.getStatut();
    }

    /** Les 160 premiers caractères du message, pour la carte d'une petite annonce. */
    private static String extrait(Annonce a) {
        if (!a.estMessage() || a.getDescription() == null) {
            return null;
        }
        String texte = a.getDescription().replaceAll("\\s+", " ").strip();
        return texte.length() <= 160 ? texte : texte.substring(0, 159) + "…";
    }

    private static String photoPrincipale(Annonce a) {
        return a.getPhotos().isEmpty() ? null : a.getPhotos().get(0).getUrl();
    }
}

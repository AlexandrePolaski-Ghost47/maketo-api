package com.maketo.annonce.service;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.dto.AnnonceRequest;
import com.maketo.annonce.dto.AnnonceResumeResponse;
import com.maketo.annonce.dto.ContactResponse;
import com.maketo.annonce.dto.CriteresRecherche;
import com.maketo.annonce.dto.MonAnnonceResponse;
import com.maketo.annonce.dto.VitrineResponse;
import com.maketo.annonce.entity.Annonce;
import com.maketo.annonce.entity.StatutAnnonce;
import com.maketo.annonce.mapper.AnnonceMapper;
import com.maketo.annonce.policy.PolitiquePublication;
import com.maketo.annonce.policy.SelecteurPolitique;
import com.maketo.annonce.repository.AnnonceRepository;
import com.maketo.annonce.specification.AnnonceSpecifications;
import com.maketo.categorie.entity.Categorie;
import com.maketo.categorie.service.CategorieService;
import com.maketo.common.exception.CompteNonActifException;
import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnnonceServiceImpl implements AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CategorieService categorieService;
    private final SelecteurPolitique selecteurPolitique;
    private final AnnonceMapper annonceMapper;
    private final AnnonceChargeur chargeur;
    private final Clock clock;

    // ------------------------------------------------------------------
    // Lecture publique
    // ------------------------------------------------------------------

    @Override
    public Page<AnnonceResumeResponse> rechercher(CriteresRecherche c, Pageable pageable) {
        Instant maintenant = clock.instant();
        Specification<Annonce> spec = AnnonceSpecifications.visibles(maintenant);
        if (rempli(c.categorie())) spec = spec.and(AnnonceSpecifications.categorie(c.categorie()));
        if (rempli(c.q())) spec = spec.and(AnnonceSpecifications.texte(c.q()));
        if (c.prixMin() != null) spec = spec.and(AnnonceSpecifications.prixMin(c.prixMin()));
        if (c.prixMax() != null) spec = spec.and(AnnonceSpecifications.prixMax(c.prixMax()));
        if (c.etat() != null) spec = spec.and(AnnonceSpecifications.etat(c.etat()));
        if (c.type() != null) spec = spec.and(AnnonceSpecifications.type(c.type()));

        return annonceRepository.findAll(spec, pageable).map(a -> annonceMapper.versResume(a, maintenant));
    }

    @Override
    public List<AnnonceResumeResponse> aLaUne(int limite) {
        Instant maintenant = clock.instant();
        return annonceRepository.findALaUne(maintenant, PageRequest.of(0, limite)).stream()
                .map(a -> annonceMapper.versResume(a, maintenant))
                .toList();
    }

    @Override
    @Transactional
    public AnnonceDetailResponse consulter(Long annonceId, Long lecteurId, boolean lecteurAdmin) {
        Instant maintenant = clock.instant();
        Annonce annonce = chargeur.charger(annonceId);
        boolean proprietaire = lecteurId != null && annonce.appartientA(lecteurId);

        // Une annonce non visible n'existe pas pour le public (seuls son vendeur et l'admin la voient)
        if (!annonce.estVisible(maintenant) && !proprietaire && !lecteurAdmin) {
            throw new RessourceIntrouvableException("Annonce introuvable");
        }
        if (!proprietaire) {
            annonceRepository.incrementerVues(annonceId);
        }
        return annonceMapper.versDetail(annonce, lecteurId != null, maintenant);
    }

    @Override
    @Transactional
    public ContactResponse contacter(Long annonceId, Long utilisateurId) {
        Annonce annonce = chargeur.charger(annonceId);
        if (!annonce.estVisible(clock.instant())) {
            throw new RessourceIntrouvableException("Annonce introuvable");
        }
        if (!annonce.appartientA(utilisateurId)) {
            annonceRepository.incrementerContacts(annonceId);
        }
        Utilisateur vendeur = annonce.getVendeur();
        String message = "Bonjour, je suis intéressé(e) par votre annonce « " + annonce.getTitre()
                + " » vue sur Maketo.";
        String lien = "https://wa.me/237" + vendeur.getWhatsapp() + "?text="
                + URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        return new ContactResponse(lien, vendeur.getTelephone());
    }

    // ------------------------------------------------------------------
    // Vendeur
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AnnonceDetailResponse publier(Long vendeurId, AnnonceRequest r) {
        Instant maintenant = clock.instant();
        Utilisateur vendeur = chargerVendeurActif(vendeurId);
        Categorie categorie = categorieService.chargerActive(r.categorieId());

        PolitiquePublication politique = selecteurPolitique.pour(vendeur, maintenant);
        politique.verifierDroit(vendeur, null, maintenant);

        Annonce annonce = Annonce.publier(vendeur, categorie, r.type(), nettoyer(r.titre()), nettoyer(r.description()),
                r.prix(), r.prixADebattre(), r.etat(), nettoyer(r.lieu()),
                politique.formule(), politique.dateExpiration(maintenant));

        return annonceMapper.versDetail(annonceRepository.save(annonce), true, maintenant);
    }

    @Override
    @Transactional
    public AnnonceDetailResponse modifier(Long annonceId, Long utilisateurId, AnnonceRequest r) {
        Annonce annonce = chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        Categorie categorie = categorieService.chargerActive(r.categorieId());
        annonce.modifier(categorie, nettoyer(r.titre()), nettoyer(r.description()), r.prix(), r.prixADebattre(),
                r.etat(), nettoyer(r.lieu()));
        return annonceMapper.versDetail(annonce, true, clock.instant());
    }

    @Override
    @Transactional
    public AnnonceDetailResponse marquerVendue(Long annonceId, Long utilisateurId) {
        Annonce annonce = chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        annonce.marquerVendue();
        return annonceMapper.versDetail(annonce, true, clock.instant());
    }

    @Override
    @Transactional
    public AnnonceDetailResponse renouveler(Long annonceId, Long utilisateurId) {
        Instant maintenant = clock.instant();
        Annonce annonce = chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        if (annonce.estVisible(maintenant)) {
            throw new MetierException("Ton annonce est encore visible, tu pourras la renouveler quand elle aura expiré");
        }
        Utilisateur vendeur = chargerVendeurActif(utilisateurId);
        PolitiquePublication politique = selecteurPolitique.pour(vendeur, maintenant);
        politique.verifierDroit(vendeur, annonce, maintenant);

        annonce.renouveler(politique.formule(), politique.dateExpiration(maintenant), maintenant);
        return annonceMapper.versDetail(annonce, true, maintenant);
    }

    @Override
    @Transactional
    public void supprimer(Long annonceId, Long utilisateurId, boolean admin) {
        Annonce annonce = admin ? chargeur.charger(annonceId) : chargeur.chargerPourProprietaire(annonceId, utilisateurId);
        annonce.supprimer();
    }

    @Override
    public Page<MonAnnonceResponse> mesAnnonces(Long vendeurId, Pageable pageable) {
        Instant maintenant = clock.instant();
        Pageable trie = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "dateCreation"));
        return annonceRepository.findByVendeurIdAndStatutNot(vendeurId, StatutAnnonce.SUPPRIMEE, trie)
                .map(a -> annonceMapper.versMonAnnonce(a, maintenant));
    }

    // ------------------------------------------------------------------
    // Vitrine vendeur
    // ------------------------------------------------------------------

    @Override
    public VitrineResponse vitrine(Long vendeurId, boolean lecteurConnecte) {
        Instant maintenant = clock.instant();
        Utilisateur vendeur = chargerVendeurPublic(vendeurId);
        long enLigne = annonceRepository.count(
                AnnonceSpecifications.visibles(maintenant).and(AnnonceSpecifications.vendeur(vendeurId)));
        return new VitrineResponse(annonceMapper.versVendeur(vendeur, lecteurConnecte, maintenant),
                vendeur.getDateCreation(), enLigne);
    }

    @Override
    public Page<AnnonceResumeResponse> annoncesDuVendeur(Long vendeurId, Pageable pageable) {
        Instant maintenant = clock.instant();
        chargerVendeurPublic(vendeurId);
        Pageable trie = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "dateCreation"));
        return annonceRepository.findAll(
                        AnnonceSpecifications.visibles(maintenant).and(AnnonceSpecifications.vendeur(vendeurId)), trie)
                .map(a -> annonceMapper.versResume(a, maintenant));
    }

    /** Un compte en attente ou bloqué n'a pas de vitrine publique. */
    private Utilisateur chargerVendeurPublic(Long vendeurId) {
        return utilisateurRepository.findById(vendeurId)
                .filter(Utilisateur::estActif)
                .orElseThrow(() -> new RessourceIntrouvableException("Vendeur introuvable"));
    }

    // ------------------------------------------------------------------
    // Outils
    // ------------------------------------------------------------------

    /** Un compte bloqué garde un jeton valide au plus 15 min : on revérifie avant toute publication. */
    private Utilisateur chargerVendeurActif(Long vendeurId) {
        Utilisateur vendeur = utilisateurRepository.findById(vendeurId)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
        if (!vendeur.estActif()) {
            throw new CompteNonActifException("Ton compte n'est pas actif");
        }
        return vendeur;
    }

    private static boolean rempli(String s) {
        return s != null && !s.isBlank();
    }

    private static String nettoyer(String s) {
        return rempli(s) ? s.trim() : null;
    }
}

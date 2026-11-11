package com.maketo.annonce.controller;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.dto.AnnonceRequest;
import com.maketo.annonce.dto.AnnonceResumeResponse;
import com.maketo.annonce.dto.ContactResponse;
import com.maketo.annonce.dto.CriteresRecherche;
import com.maketo.annonce.dto.TriAnnonce;
import com.maketo.annonce.entity.EtatArticle;
import com.maketo.annonce.entity.TypeAnnonce;
import com.maketo.annonce.service.AnnonceService;
import com.maketo.common.dto.PageResponse;
import com.maketo.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET = public (visiteurs compris). Toutes les autres méthodes exigent d'être connecté (voir SecurityConfig).
 */
@RestController
@RequestMapping("/api/annonces")
@RequiredArgsConstructor
public class AnnonceController {

    private static final int TAILLE_MAX_PAGE = 50;

    private final AnnonceService annonceService;

    @GetMapping
    public PageResponse<AnnonceResumeResponse> rechercher(
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer prixMin,
            @RequestParam(required = false) Integer prixMax,
            @RequestParam(required = false) EtatArticle etat,
            @RequestParam(required = false) TypeAnnonce type,
            @RequestParam(defaultValue = "RECENT") TriAnnonce tri,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int taille) {
        PageRequest pageable = PageRequest.of(Math.max(page, 0),
                Math.min(Math.max(taille, 1), TAILLE_MAX_PAGE), tri.sort());
        CriteresRecherche criteres = new CriteresRecherche(categorie, q, prixMin, prixMax, etat, type);
        return PageResponse.de(annonceService.rechercher(criteres, pageable));
    }

    @GetMapping("/a-la-une")
    public List<AnnonceResumeResponse> aLaUne() {
        return annonceService.aLaUne(10);
    }

    @GetMapping("/{id}")
    public AnnonceDetailResponse consulter(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return annonceService.consulter(id, SecurityUtils.idUtilisateurOuNull(jwt), SecurityUtils.estAdmin(jwt));
    }

    @PostMapping("/{id}/contact")
    public ContactResponse contacter(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return annonceService.contacter(id, SecurityUtils.idUtilisateur(jwt));
    }

    @PostMapping
    public ResponseEntity<AnnonceDetailResponse> publier(@Valid @RequestBody AnnonceRequest requete,
                                                         @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(annonceService.publier(SecurityUtils.idUtilisateur(jwt), requete));
    }

    @PutMapping("/{id}")
    public AnnonceDetailResponse modifier(@PathVariable Long id, @Valid @RequestBody AnnonceRequest requete,
                                          @AuthenticationPrincipal Jwt jwt) {
        return annonceService.modifier(id, SecurityUtils.idUtilisateur(jwt), requete);
    }

    @PatchMapping("/{id}/vendue")
    public AnnonceDetailResponse marquerVendue(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return annonceService.marquerVendue(id, SecurityUtils.idUtilisateur(jwt));
    }

    @PatchMapping("/{id}/renouveler")
    public AnnonceDetailResponse renouveler(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return annonceService.renouveler(id, SecurityUtils.idUtilisateur(jwt));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        annonceService.supprimer(id, SecurityUtils.idUtilisateur(jwt), SecurityUtils.estAdmin(jwt));
        return ResponseEntity.noContent().build();
    }
}

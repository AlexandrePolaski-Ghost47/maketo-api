package com.maketo.annonce.controller;

import com.maketo.annonce.dto.AnnonceDetailResponse;
import com.maketo.annonce.service.PhotoAnnonceService;
import com.maketo.common.exception.MetierException;
import com.maketo.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/annonces/{annonceId}/photos")
@RequiredArgsConstructor
public class PhotoAnnonceController {

    private final PhotoAnnonceService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnnonceDetailResponse> ajouter(@PathVariable Long annonceId,
                                                         @RequestParam("fichier") MultipartFile fichier,
                                                         @AuthenticationPrincipal Jwt jwt) throws IOException {
        if (fichier.isEmpty()) {
            throw new MetierException("Aucune photo reçue");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(photoService.ajouter(annonceId, SecurityUtils.idUtilisateur(jwt), fichier.getBytes()));
    }

    @DeleteMapping("/{photoId}")
    public AnnonceDetailResponse supprimer(@PathVariable Long annonceId, @PathVariable Long photoId,
                                           @AuthenticationPrincipal Jwt jwt) {
        return photoService.supprimer(annonceId, photoId, SecurityUtils.idUtilisateur(jwt));
    }
}

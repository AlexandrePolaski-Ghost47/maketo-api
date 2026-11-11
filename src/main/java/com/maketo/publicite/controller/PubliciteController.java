package com.maketo.publicite.controller;

import com.maketo.common.dto.PageResponse;
import com.maketo.common.exception.MetierException;
import com.maketo.publicite.dto.PubliciteAdminResponse;
import com.maketo.publicite.dto.PubliciteRequest;
import com.maketo.publicite.dto.PubliciteResponse;
import com.maketo.publicite.service.PubliciteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PubliciteController {

    private final PubliciteService publiciteService;

    // --- Public ---

    @GetMapping("/api/publicites")
    public List<PubliciteResponse> actives() {
        return publiciteService.actives();
    }

    @PostMapping("/api/publicites/{id}/clic")
    public ResponseEntity<Void> clic(@PathVariable Long id) {
        publiciteService.enregistrerClic(id);
        return ResponseEntity.noContent().build();
    }

    // --- Admin ---

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/api/admin/publicites", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PubliciteAdminResponse> creer(@Valid @ModelAttribute PubliciteRequest requete,
                                                        @RequestParam("image") MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new MetierException("L'image est obligatoire");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(publiciteService.creer(requete, image.getBytes()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/admin/publicites")
    public PageResponse<PubliciteAdminResponse> lister(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.de(publiciteService.lister(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/admin/publicites/{id}/activer")
    public PubliciteAdminResponse activer(@PathVariable Long id) {
        return publiciteService.changerActivation(id, true);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/admin/publicites/{id}/desactiver")
    public PubliciteAdminResponse desactiver(@PathVariable Long id) {
        return publiciteService.changerActivation(id, false);
    }
}

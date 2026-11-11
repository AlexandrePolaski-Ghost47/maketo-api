package com.maketo.categorie.controller;

import com.maketo.categorie.dto.CategorieAdminResponse;
import com.maketo.categorie.dto.CategorieRequest;
import com.maketo.categorie.service.CategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminCategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public List<CategorieAdminResponse> lister() {
        return categorieService.listerToutes();
    }

    @PostMapping
    public ResponseEntity<CategorieAdminResponse> creer(@Valid @RequestBody CategorieRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.creer(requete));
    }

    @PutMapping("/{id}")
    public CategorieAdminResponse modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest requete) {
        return categorieService.modifier(id, requete);
    }

    @PatchMapping("/{id}/activer")
    public CategorieAdminResponse activer(@PathVariable Long id) {
        return categorieService.changerActivation(id, true);
    }

    @PatchMapping("/{id}/desactiver")
    public CategorieAdminResponse desactiver(@PathVariable Long id) {
        return categorieService.changerActivation(id, false);
    }
}

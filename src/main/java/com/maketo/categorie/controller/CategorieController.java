package com.maketo.categorie.controller;

import com.maketo.categorie.dto.CategorieResponse;
import com.maketo.categorie.service.CategorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public List<CategorieResponse> lister() {
        return categorieService.listerActives();
    }
}

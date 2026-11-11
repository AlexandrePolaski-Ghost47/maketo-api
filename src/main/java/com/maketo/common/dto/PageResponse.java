package com.maketo.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Format de pagination stable exposé par l'API (on ne sérialise jamais Page directement). */
public record PageResponse<T>(List<T> contenu, int page, int taille, long totalElements, int totalPages) {

    public static <T> PageResponse<T> de(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}

package com.maketo.publicite.service;

import com.maketo.publicite.dto.PubliciteAdminResponse;
import com.maketo.publicite.dto.PubliciteRequest;
import com.maketo.publicite.dto.PubliciteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PubliciteService {

    List<PubliciteResponse> actives();

    void enregistrerClic(Long id);

    PubliciteAdminResponse creer(PubliciteRequest requete, byte[] image);

    Page<PubliciteAdminResponse> lister(Pageable pageable);

    PubliciteAdminResponse changerActivation(Long id, boolean actif);
}

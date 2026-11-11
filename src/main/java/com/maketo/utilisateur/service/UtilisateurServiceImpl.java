package com.maketo.utilisateur.service;

import com.maketo.common.exception.MetierException;
import com.maketo.common.exception.RessourceIntrouvableException;
import com.maketo.common.util.TelephoneUtils;
import com.maketo.utilisateur.dto.MotDePasseRequest;
import com.maketo.utilisateur.dto.ProfilRequest;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.entity.StatutUtilisateur;
import com.maketo.utilisateur.entity.Utilisateur;
import com.maketo.utilisateur.event.MotDePasseModifieEvent;
import com.maketo.utilisateur.event.UtilisateurBloqueEvent;
import com.maketo.utilisateur.mapper.UtilisateurMapper;
import com.maketo.utilisateur.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final ApplicationEventPublisher evenements;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UtilisateurResponse profil(Long utilisateurId) {
        return utilisateurMapper.versResponse(charger(utilisateurId));
    }

    @Override
    public Page<UtilisateurResponse> listerParStatut(StatutUtilisateur statut, Pageable pageable) {
        return utilisateurRepository.findByStatut(statut, pageable).map(utilisateurMapper::versResponse);
    }

    @Override
    @Transactional
    public UtilisateurResponse activer(Long utilisateurId) {
        Utilisateur utilisateur = charger(utilisateurId);
        utilisateur.activer();
        return utilisateurMapper.versResponse(utilisateur);
    }

    @Override
    @Transactional
    public UtilisateurResponse bloquer(Long utilisateurId, Long adminId) {
        if (utilisateurId.equals(adminId)) {
            throw new MetierException("Tu ne peux pas bloquer ton propre compte");
        }
        Utilisateur utilisateur = charger(utilisateurId);
        if (utilisateur.estAdmin()) {
            throw new MetierException("Un compte administrateur ne peut pas être bloqué");
        }
        utilisateur.bloquer();
        evenements.publishEvent(new UtilisateurBloqueEvent(utilisateurId));
        return utilisateurMapper.versResponse(utilisateur);
    }

    @Override
    @Transactional
    public UtilisateurResponse modifierProfil(Long utilisateurId, ProfilRequest r) {
        Utilisateur utilisateur = charger(utilisateurId);
        utilisateur.setNom(r.nom().trim());
        utilisateur.setPrenom(r.prenom().trim());
        if (r.whatsapp() != null && !r.whatsapp().isBlank()) {
            utilisateur.setWhatsapp(TelephoneUtils.normaliser(r.whatsapp()));
        }
        utilisateur.setFiliere(r.filiere());
        utilisateur.setNiveau(r.niveau());
        return utilisateurMapper.versResponse(utilisateur);
    }

    @Override
    @Transactional
    public void changerMotDePasse(Long utilisateurId, MotDePasseRequest r) {
        Utilisateur utilisateur = charger(utilisateurId);
        if (!passwordEncoder.matches(r.ancien(), utilisateur.getMotDePasse())) {
            throw new MetierException("Mot de passe actuel incorrect");
        }
        if (r.ancien().equals(r.nouveau())) {
            throw new MetierException("Le nouveau mot de passe doit être différent de l'ancien");
        }
        utilisateur.changerMotDePasse(passwordEncoder.encode(r.nouveau()));
        evenements.publishEvent(new MotDePasseModifieEvent(utilisateurId));
    }

    private Utilisateur charger(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur introuvable"));
    }
}

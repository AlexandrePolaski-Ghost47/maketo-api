package com.maketo.utilisateur.mapper;

import com.maketo.common.mapper.MapStructConfig;
import com.maketo.utilisateur.dto.UtilisateurResponse;
import com.maketo.utilisateur.entity.Utilisateur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface UtilisateurMapper {

    @Mapping(target = "pro", expression = "java(utilisateur.estPro())")
    @Mapping(target = "dateInscription", source = "dateCreation")
    UtilisateurResponse versResponse(Utilisateur utilisateur);
}

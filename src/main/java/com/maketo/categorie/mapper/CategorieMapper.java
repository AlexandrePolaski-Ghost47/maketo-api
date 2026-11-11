package com.maketo.categorie.mapper;

import com.maketo.categorie.dto.CategorieAdminResponse;
import com.maketo.categorie.dto.CategorieResponse;
import com.maketo.categorie.entity.Categorie;
import com.maketo.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface CategorieMapper {

    CategorieResponse versResponse(Categorie categorie);

    List<CategorieResponse> versResponses(List<Categorie> categories);

    CategorieAdminResponse versAdmin(Categorie categorie);
}

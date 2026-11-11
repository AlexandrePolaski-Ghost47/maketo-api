package com.maketo.credit.repository;

import com.maketo.credit.entity.Offre;
import com.maketo.credit.entity.TypeOffre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OffreRepository extends JpaRepository<Offre, Long> {

    Optional<Offre> findByCodeAndActifTrue(String code);

    List<Offre> findByTypeAndActifTrueOrderByPrixAsc(TypeOffre type);

    List<Offre> findByActifTrueOrderByTypeAscPrixAsc();
}

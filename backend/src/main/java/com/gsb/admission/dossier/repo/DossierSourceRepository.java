package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.model.DossierSourceEntity;
import com.gsb.admission.dossier.model.DossierSourceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DossierSourceRepository extends JpaRepository<DossierSourceEntity, DossierSourceId> {

    List<DossierSourceEntity> findByDossierIdOrderByPositionAsc(UUID dossierId);

    Optional<DossierSourceEntity> findByDossierIdAndSourceId(UUID dossierId, String sourceId);
}

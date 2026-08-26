package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.model.DossierSealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DossierSealRepository extends JpaRepository<DossierSealEntity, UUID> {

    List<DossierSealEntity> findByDossierIdOrderBySealedAtAsc(UUID dossierId);

    Optional<DossierSealEntity> findBySealIdAndDossierId(UUID sealId, UUID dossierId);

    boolean existsByDossierIdAndAsOf(UUID dossierId, Instant asOf);
}

package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.model.SourceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceRecordRepository extends JpaRepository<SourceRecordEntity, UUID> {

    List<SourceRecordEntity> findByDossierIdOrderByRecordedAtAsc(UUID dossierId);

    List<SourceRecordEntity> findByDossierIdAndSourceIdOrderBySourceRevisionAsc(UUID dossierId, String sourceId);

    Optional<SourceRecordEntity> findByDossierIdAndSourceIdAndSourceRevision(UUID dossierId, String sourceId, int sourceRevision);

    boolean existsByDossierIdAndSourceIdAndSourceRevision(UUID dossierId, String sourceId, int sourceRevision);

    boolean existsByDossierIdAndRecordedAtAfter(UUID dossierId, Instant asOf);
}

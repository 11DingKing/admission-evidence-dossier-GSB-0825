package com.dossier.admission.repository;

import com.dossier.admission.domain.DossierEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DossierEntryRepository extends JpaRepository<DossierEntry, Long> {
    List<DossierEntry> findByDossierIdOrderBySourceId(String dossierId);
}

package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.model.SourceRegistryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SourceRegistryRepository extends JpaRepository<SourceRegistryEntity, String> {

    List<SourceRegistryEntity> findAllBySourceIdIn(Collection<String> sourceIds);
}

package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.domain.SubjectCategory;
import com.gsb.admission.dossier.model.DossierEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DossierRepository extends JpaRepository<DossierEntity, UUID> {

    Optional<DossierEntity> findByProvinceCodeAndAdmissionYearAndSubjectCategoryAndSchoolCodeAndMajorGroupCode(
            String provinceCode,
            int admissionYear,
            SubjectCategory subjectCategory,
            String schoolCode,
            String majorGroupCode);

    List<DossierEntity> findAllByOrderByCreatedAtAsc();
}

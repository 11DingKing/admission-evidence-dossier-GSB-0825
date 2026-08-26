package com.gsb.admission.dossier.repo;

import com.gsb.admission.dossier.domain.SubjectCategory;
import com.gsb.admission.dossier.model.SourcePublicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface SourcePublicationRepository extends JpaRepository<SourcePublicationEntity, UUID> {

    Optional<SourcePublicationEntity> findTopBySourceIdAndAdmissionYearAndRecordedAtLessThanEqualOrderBySourceRevisionDesc(
            String sourceId,
            int admissionYear,
            Instant recordedAt);

    Optional<SourcePublicationEntity> findTopBySourceIdAndProvinceCodeAndAdmissionYearAndSubjectCategoryAndSchoolCodeAndMajorGroupCodeAndRecordedAtLessThanEqualOrderBySourceRevisionDesc(
            String sourceId,
            String provinceCode,
            int admissionYear,
            SubjectCategory subjectCategory,
            String schoolCode,
            String majorGroupCode,
            Instant recordedAt);
}

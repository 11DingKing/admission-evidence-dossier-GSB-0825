package com.gsb.admission.dossier.service;

import com.gsb.admission.dossier.domain.FactKey;
import com.gsb.admission.dossier.model.SourcePublicationEntity;
import com.gsb.admission.dossier.repo.SourcePublicationRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PublicationTableSourceAdapter implements SourceAdapter {

    private final SourcePublicationRepository publicationRepository;

    public PublicationTableSourceAdapter(SourcePublicationRepository publicationRepository) {
        this.publicationRepository = publicationRepository;
    }

    @Override
    public Optional<SourcePublicationPayload> fetch(String sourceId, FactKey factKey, Instant asOf) {
        Optional<SourcePublicationEntity> exact = publicationRepository
                .findTopBySourceIdAndProvinceCodeAndAdmissionYearAndSubjectCategoryAndSchoolCodeAndMajorGroupCodeAndRecordedAtLessThanEqualOrderBySourceRevisionDesc(
                        sourceId, factKey.provinceCode(), factKey.admissionYear(), factKey.subjectCategory(),
                        factKey.schoolCode(), factKey.majorGroupCode(), asOf);
        SourcePublicationEntity pub = exact.orElseGet(() -> publicationRepository
                .findTopBySourceIdAndAdmissionYearAndRecordedAtLessThanEqualOrderBySourceRevisionDesc(
                        sourceId, factKey.admissionYear(), asOf)
                .orElse(null));
        if (pub == null) {
            return Optional.empty();
        }
        if (!pub.isAvailable()) {
            throw new SourceUnavailableException(pub.getErrorCode(),
                    "source " + sourceId + " is unavailable"
                            + (pub.getErrorCode() != null ? " (" + pub.getErrorCode() + ")" : ""));
        }
        return Optional.of(new SourcePublicationPayload(
                pub.getSourceId(),
                pub.getProvinceCode(),
                pub.getAdmissionYear(),
                pub.getSubjectCategory(),
                pub.getSchoolCode(),
                pub.getMajorGroupCode(),
                pub.getSourceRevision(),
                pub.getScoreTenths(),
                pub.getScoreScale(),
                pub.getSourceDocNo(),
                pub.getEffectiveFrom(),
                pub.getEffectiveTo(),
                pub.getRecordedAt(),
                pub.getRawText()));
    }
}

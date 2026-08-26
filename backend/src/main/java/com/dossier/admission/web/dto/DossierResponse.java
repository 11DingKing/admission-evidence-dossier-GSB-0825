package com.dossier.admission.web.dto;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.DossierStatus;
import java.time.Instant;
import java.util.List;

public record DossierResponse(
        String dossierId,
        String provinceCode,
        Integer admissionYear,
        String subjectCategory,
        String schoolCode,
        String majorGroupCode,
        String expectedScale,
        DossierStatus status,
        Instant asOf,
        boolean sealed,
        Instant sealedAt,
        Instant createdAt,
        Instant updatedAt,
        boolean expired,
        List<SourceRecordResponse> sources,
        List<DossierEntryResponse> sealedEntries
) {
    public static DossierResponse from(Dossier dossier, List<SourceRecordResponse> sources,
                                        List<DossierEntryResponse> entries, boolean expired) {
        return new DossierResponse(
                dossier.getDossierId(),
                dossier.getProvinceCode(),
                dossier.getAdmissionYear(),
                dossier.getSubjectCategory(),
                dossier.getSchoolCode(),
                dossier.getMajorGroupCode(),
                dossier.getExpectedScale(),
                dossier.getStatus(),
                dossier.getAsOf(),
                dossier.isSealed(),
                dossier.getSealedAt(),
                dossier.getCreatedAt(),
                dossier.getUpdatedAt(),
                expired,
                sources,
                entries
        );
    }
}

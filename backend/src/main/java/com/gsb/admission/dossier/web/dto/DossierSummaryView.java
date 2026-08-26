package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;
import com.gsb.admission.dossier.domain.ScoreScale;
import com.gsb.admission.dossier.domain.SubjectCategory;

import java.time.Instant;

public record DossierSummaryView(
        String dossierId,
        String provinceCode,
        int admissionYear,
        SubjectCategory subjectCategory,
        String schoolCode,
        String majorGroupCode,
        ScoreScale expectedScale,
        DossierStatus status,
        int sourceCount,
        Instant createdAt) {
}

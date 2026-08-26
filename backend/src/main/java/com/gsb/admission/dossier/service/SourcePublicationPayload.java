package com.gsb.admission.dossier.service;

import com.gsb.admission.dossier.domain.FactKey;
import com.gsb.admission.dossier.domain.ScoreScale;
import com.gsb.admission.dossier.domain.SubjectCategory;

import java.time.Instant;
import java.time.LocalDate;

public record SourcePublicationPayload(
        String sourceId,
        String provinceCode,
        int admissionYear,
        SubjectCategory subjectCategory,
        String schoolCode,
        String majorGroupCode,
        int sourceRevision,
        long scoreTenths,
        ScoreScale scoreScale,
        String sourceDocNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Instant recordedAt,
        String rawText) {

    public FactKey factKey() {
        return new FactKey(provinceCode, admissionYear, subjectCategory, schoolCode, majorGroupCode);
    }
}

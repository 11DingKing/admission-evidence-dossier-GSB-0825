package com.dossier.admission.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record SourceRecordRequest(
        @NotBlank String provinceCode,
        @NotNull Integer admissionYear,
        @NotBlank String subjectCategory,
        @NotBlank String schoolCode,
        @NotBlank String majorGroupCode,
        @NotBlank String sourceId,
        @NotNull Integer sourceRevision,
        @NotNull @PositiveOrZero Long scoreValue,
        @NotBlank String scoreScale,
        String originalScoreText,
        @NotNull Instant effectiveFrom,
        Instant effectiveTo,
        @NotNull Instant recordedAt,
        String sourceDocumentNo,
        String originalText
) {
}

package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.ScoreScale;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;

public record ManualSourceRecordRequest(
        @NotBlank String sourceId,
        @Min(1) int sourceRevision,
        long scoreTenths,
        @NotNull ScoreScale scoreScale,
        @NotBlank String sourceDocNo,
        @NotNull LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Instant recordedAt,
        @NotBlank String rawText) {
}

package com.gsb.admission.dossier.domain;

import java.time.Instant;
import java.time.LocalDate;

public record FactEvidence(
        String sourceId,
        int sourceRevision,
        long scoreTenths,
        ScoreScale scoreScale,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Instant recordedAt) {
}

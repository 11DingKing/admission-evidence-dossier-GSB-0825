package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;

import java.time.Instant;
import java.util.UUID;

public record SealSummaryView(
        UUID sealId,
        Instant asOf,
        DossierStatus status,
        Instant sealedAt,
        String contentHash,
        boolean hasNewerEvidence) {
}

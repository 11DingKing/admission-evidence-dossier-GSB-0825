package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SealView(
        UUID sealId,
        Instant asOf,
        DossierStatus status,
        Instant sealedAt,
        String contentHash,
        boolean hasNewerEvidence,
        UUID dossierId,
        FactKeyView factKey,
        Map<String, Object> snapshot) {
}

package com.dossier.admission.web.dto;

import com.dossier.admission.domain.DossierEntry;
import com.dossier.admission.domain.Freshness;
import java.time.Instant;

public record DossierEntryResponse(
        String sourceId,
        Integer sourceRevision,
        Long scoreValue,
        String scoreScale,
        String originalScoreText,
        Instant effectiveFrom,
        Instant effectiveTo,
        Instant recordedAt,
        String sourceDocumentNo,
        String originalText,
        String contentHash,
        Freshness freshness
) {
    public static DossierEntryResponse from(DossierEntry entry, Freshness freshness) {
        return new DossierEntryResponse(
                entry.getSourceId(),
                entry.getSourceRevision(),
                entry.getScoreValue(),
                entry.getScoreScale(),
                entry.getOriginalScoreText(),
                entry.getEffectiveFrom(),
                entry.getEffectiveTo(),
                entry.getRecordedAt(),
                entry.getSourceDocumentNo(),
                entry.getOriginalText(),
                entry.getContentHash(),
                freshness
        );
    }
}

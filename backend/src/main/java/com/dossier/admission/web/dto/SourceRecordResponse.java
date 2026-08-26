package com.dossier.admission.web.dto;

import com.dossier.admission.domain.Freshness;
import com.dossier.admission.domain.SourceRecord;
import java.time.Instant;

public record SourceRecordResponse(
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
        Freshness freshness,
        boolean isLatestRevision
) {
    public static SourceRecordResponse from(SourceRecord record, Freshness freshness, boolean isLatest) {
        return new SourceRecordResponse(
                record.getSourceId(),
                record.getSourceRevision(),
                record.getScoreValue(),
                record.getScoreScale(),
                record.getOriginalScoreText(),
                record.getEffectiveFrom(),
                record.getEffectiveTo(),
                record.getRecordedAt(),
                record.getSourceDocumentNo(),
                record.getOriginalText(),
                record.getContentHash(),
                freshness,
                isLatest
        );
    }
}

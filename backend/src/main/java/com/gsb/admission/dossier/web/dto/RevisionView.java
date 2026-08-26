package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.Freshness;
import com.gsb.admission.dossier.domain.Origin;
import com.gsb.admission.dossier.domain.ScoreScale;

import java.time.Instant;
import java.time.LocalDate;

public record RevisionView(
        String sourceId,
        String sourceName,
        int sourceRevision,
        long scoreTenths,
        String scoreDisplay,
        ScoreScale scoreScale,
        String sourceDocNo,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        Instant recordedAt,
        Freshness freshness,
        String contentHash,
        String rawText,
        Origin origin,
        boolean superseded,
        Long deltaTenths) {

    public static RevisionView from(SourceRecordView r, Long deltaTenths) {
        return new RevisionView(
                r.sourceId(),
                r.sourceName(),
                r.sourceRevision(),
                r.scoreTenths(),
                r.scoreDisplay(),
                r.scoreScale(),
                r.sourceDocNo(),
                r.effectiveFrom(),
                r.effectiveTo(),
                r.recordedAt(),
                r.freshness(),
                r.contentHash(),
                r.rawText(),
                r.origin(),
                r.superseded(),
                deltaTenths);
    }
}

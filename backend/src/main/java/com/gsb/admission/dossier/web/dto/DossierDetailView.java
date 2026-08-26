package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;
import com.gsb.admission.dossier.domain.ScoreScale;

import java.time.Instant;
import java.util.List;

public record DossierDetailView(
        String dossierId,
        FactKeyView factKey,
        String factKeyHash,
        ScoreScale expectedScale,
        DossierStatus status,
        Instant asOf,
        List<SourceStateView> sources,
        List<SourceRecordView> evidenceChain,
        List<SealSummaryView> seals) {
}

package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;

import java.util.List;

public record SnapshotView(
        DossierStatus status,
        List<SourceStateView> sources,
        List<SourceRecordView> evidenceChain) {
}

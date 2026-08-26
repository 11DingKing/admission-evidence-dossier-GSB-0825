package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.LoadState;

public record SourceStateView(
        String sourceId,
        String sourceName,
        LoadState loadState,
        String errorCode,
        String errorMessage,
        SourceRecordView current,
        int revisionCount) {
}

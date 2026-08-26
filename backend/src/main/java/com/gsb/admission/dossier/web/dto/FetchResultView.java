package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.FetchOutcome;

public record FetchResultView(
        String sourceId,
        FetchOutcome outcome,
        SourceRecordView record,
        String errorCode,
        String errorMessage) {
}

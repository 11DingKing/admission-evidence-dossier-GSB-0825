package com.dossier.admission.web.dto;

import java.util.List;

public record BatchSourceResponse(
        List<SourceRecordResponse> accepted,
        List<SourceError> errors,
        int totalCount,
        int successCount,
        int failureCount,
        boolean partialFailure
) {
}

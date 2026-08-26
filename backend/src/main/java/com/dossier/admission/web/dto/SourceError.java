package com.dossier.admission.web.dto;

public record SourceError(
        int index,
        String sourceId,
        String errorCode,
        String message
) {
}

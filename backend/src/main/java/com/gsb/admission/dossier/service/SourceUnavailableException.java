package com.gsb.admission.dossier.service;

public class SourceUnavailableException extends RuntimeException {

    private final String errorCode;

    public SourceUnavailableException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode == null ? "SOURCE_UNAVAILABLE" : errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

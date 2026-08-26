package com.gsb.admission.dossier.service;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, Object> details;

    public ApiException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, Map.of());
    }

    public ApiException(HttpStatus status, String errorCode, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public static ApiException dossierNotFound(java.util.UUID dossierId) {
        return new ApiException(HttpStatus.NOT_FOUND, "DOSSIER_NOT_FOUND",
                "dossier not found: " + dossierId,
                Map.of("dossierId", dossierId.toString()));
    }

    public static ApiException sealNotFound(java.util.UUID sealId) {
        return new ApiException(HttpStatus.NOT_FOUND, "SEAL_NOT_FOUND",
                "seal not found: " + sealId,
                Map.of("sealId", sealId.toString()));
    }

    public static ApiException dossierExists() {
        return new ApiException(HttpStatus.CONFLICT, "DOSSIER_EXISTS",
                "a dossier for this fact key already exists");
    }

    public static ApiException sealConflict() {
        return new ApiException(HttpStatus.CONFLICT, "SEAL_CONFLICT",
                "a seal for this as_of already exists");
    }

    public static ApiException validation(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }
}

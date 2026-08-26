package com.dossier.admission.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDossierRequest(
        @NotBlank String provinceCode,
        @NotNull Integer admissionYear,
        @NotBlank String subjectCategory,
        @NotBlank String schoolCode,
        @NotBlank String majorGroupCode,
        @NotBlank String expectedScale
) {
}

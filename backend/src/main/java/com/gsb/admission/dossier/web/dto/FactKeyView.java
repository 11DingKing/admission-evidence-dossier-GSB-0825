package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.SubjectCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FactKeyView(
        @NotBlank String provinceCode,
        int admissionYear,
        @NotNull SubjectCategory subjectCategory,
        @NotBlank String schoolCode,
        @NotBlank String majorGroupCode) {
}

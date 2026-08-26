package com.gsb.admission.dossier.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateDossierRequest(
        @NotNull @Valid FactKeyView factKey,
        @NotNull @Size(max = 5) List<@NotBlank String> sourceIds) {
}

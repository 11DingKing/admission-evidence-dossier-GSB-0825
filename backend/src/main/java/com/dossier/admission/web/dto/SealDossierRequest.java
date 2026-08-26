package com.dossier.admission.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record SealDossierRequest(
        @NotNull Instant asOf
) {
}

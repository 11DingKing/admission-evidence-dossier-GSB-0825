package com.gsb.admission.dossier.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SealRequest(@NotNull Instant asOf) {
}

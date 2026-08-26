package com.dossier.admission.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchSourceRequest(
        @NotEmpty @Size(max = 5) @Valid List<SourceRecordRequest> sources
) {
}

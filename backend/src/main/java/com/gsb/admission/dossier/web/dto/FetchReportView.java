package com.gsb.admission.dossier.web.dto;

import com.gsb.admission.dossier.domain.DossierStatus;

import java.time.Instant;
import java.util.List;

public record FetchReportView(
        String dossierId,
        Instant asOf,
        DossierStatus dossierStatus,
        List<FetchResultView> results) {
}

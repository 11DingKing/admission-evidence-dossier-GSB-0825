package com.gsb.admission.dossier.web.dto;

import java.util.List;

public record DossierListResponse(List<DossierSummaryView> dossiers) {
}

package com.gsb.admission.dossier.web.dto;

import java.util.List;

public record RevisionChainView(String sourceId, List<RevisionView> revisions) {
}

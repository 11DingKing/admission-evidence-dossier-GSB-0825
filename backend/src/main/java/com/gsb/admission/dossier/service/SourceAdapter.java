package com.gsb.admission.dossier.service;

import com.gsb.admission.dossier.domain.FactKey;

import java.time.Instant;
import java.util.Optional;

public interface SourceAdapter {

    Optional<SourcePublicationPayload> fetch(String sourceId, FactKey factKey, Instant asOf);
}

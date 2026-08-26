package com.gsb.admission.dossier.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class RevisionPolicy {

    private RevisionPolicy() {
    }

    public static Optional<FactEvidence> currentValue(List<FactEvidence> records, Instant asOf) {
        LocalDate asOfDate = LocalDate.ofInstant(asOf, ZoneOffset.UTC);
        return records.stream()
                .filter(r -> !r.recordedAt().isAfter(asOf))
                .filter(r -> !r.effectiveFrom().isAfter(asOfDate))
                .max(Comparator.comparingInt(FactEvidence::sourceRevision));
    }

    public static boolean isSuperseded(FactEvidence record, Optional<FactEvidence> current) {
        return current.isPresent() && current.get().sourceRevision() > record.sourceRevision();
    }
}

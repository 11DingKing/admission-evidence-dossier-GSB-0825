package com.dossier.admission.service;

import com.dossier.admission.domain.DossierStatus;
import com.dossier.admission.domain.Freshness;
import com.dossier.admission.domain.SourceRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConsistencyEvaluator {

    private static final Duration FRESH_WINDOW = Duration.ofDays(30);

    public DossierStatus evaluate(List<SourceRecord> activeSources, String expectedScale) {
        if (activeSources.isEmpty()) {
            return DossierStatus.PENDING;
        }

        boolean scaleMismatch = activeSources.stream()
                .anyMatch(s -> !s.getScoreScale().equals(expectedScale));
        if (scaleMismatch) {
            return DossierStatus.SCALE_CONFLICT;
        }

        boolean allSameScale = activeSources.stream()
                .map(SourceRecord::getScoreScale)
                .distinct()
                .count() == 1;
        if (!allSameScale) {
            return DossierStatus.SCALE_CONFLICT;
        }

        long distinctValues = activeSources.stream()
                .map(SourceRecord::getScoreValue)
                .distinct()
                .count();
        if (distinctValues == 1) {
            return DossierStatus.CONSISTENT;
        }
        return DossierStatus.CONFLICT;
    }

    public Freshness freshness(SourceRecord record, Instant referenceTime) {
        Instant effectiveTo = record.getEffectiveTo();
        if (effectiveTo != null && referenceTime.isAfter(effectiveTo)) {
            return Freshness.EXPIRED;
        }
        Instant recordedAt = record.getRecordedAt();
        if (recordedAt != null && Duration.between(recordedAt, referenceTime).compareTo(FRESH_WINDOW) <= 0) {
            return Freshness.FRESH;
        }
        return Freshness.STALE;
    }
}

package com.gsb.admission.dossier.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public final class FreshnessPolicy {

    private FreshnessPolicy() {
    }

    public static Freshness freshness(LocalDate effectiveFrom, LocalDate effectiveTo, Instant asOf) {
        LocalDate asOfDate = LocalDate.ofInstant(asOf, ZoneOffset.UTC);
        if (asOfDate.isBefore(effectiveFrom)) {
            return Freshness.UPCOMING;
        }
        if (effectiveTo != null && asOfDate.isAfter(effectiveTo)) {
            return Freshness.EXPIRED;
        }
        return Freshness.FRESH;
    }
}

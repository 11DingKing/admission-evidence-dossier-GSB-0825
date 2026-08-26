package com.gsb.admission.dossier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FreshnessPolicyTest {

    private static final LocalDate EFFECTIVE_FROM = LocalDate.of(2025, 7, 19);
    private static final LocalDate EFFECTIVE_TO = LocalDate.of(2025, 8, 31);

    @Test
    void withinValidityWindowIsFresh() {
        assertThat(FreshnessPolicy.freshness(EFFECTIVE_FROM, null,
                Instant.parse("2025-07-20T12:00:00Z")))
                .isEqualTo(Freshness.FRESH);
    }

    @Test
    void beforeEffectiveFromIsUpcoming() {
        assertThat(FreshnessPolicy.freshness(EFFECTIVE_FROM, null,
                Instant.parse("2025-07-18T23:59:00Z")))
                .isEqualTo(Freshness.UPCOMING);
    }

    @Test
    void afterEffectiveToIsExpired() {
        assertThat(FreshnessPolicy.freshness(EFFECTIVE_FROM, EFFECTIVE_TO,
                Instant.parse("2025-09-01T00:00:00Z")))
                .isEqualTo(Freshness.EXPIRED);
    }

    @Test
    void boundaryDatesAreFresh() {
        assertThat(FreshnessPolicy.freshness(EFFECTIVE_FROM, EFFECTIVE_TO,
                Instant.parse("2025-07-19T00:00:00Z")))
                .isEqualTo(Freshness.FRESH);
        assertThat(FreshnessPolicy.freshness(EFFECTIVE_FROM, EFFECTIVE_TO,
                Instant.parse("2025-08-31T23:59:00Z")))
                .isEqualTo(Freshness.FRESH);
    }
}

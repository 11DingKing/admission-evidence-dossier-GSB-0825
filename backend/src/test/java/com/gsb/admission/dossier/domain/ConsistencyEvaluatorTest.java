package com.gsb.admission.dossier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsistencyEvaluatorTest {

    private static final LocalDate EFFECTIVE = LocalDate.of(2025, 7, 1);
    private static final Instant RECORDED = Instant.parse("2025-07-20T08:00:00Z");

    private static FactEvidence evidence(String sourceId, long scoreTenths, ScoreScale scale) {
        return new FactEvidence(sourceId, 1, scoreTenths, scale, EFFECTIVE, null, RECORDED);
    }

    @Test
    void conflictingScoresYieldConflict() {
        List<FactEvidence> currents = List.of(
                evidence("GD_EDU_EXAM", 6100, ScoreScale.GAOKAO_750_TENTH),
                evidence("SZPU_ADMISSION", 6090, ScoreScale.GAOKAO_750_TENTH));

        assertThat(ConsistencyEvaluator.evaluate(currents, ScoreScale.GAOKAO_750_TENTH))
                .isEqualTo(DossierStatus.CONFLICT);
    }

    @Test
    void sameScoreAndScaleYieldConsistent() {
        List<FactEvidence> currents = List.of(
                evidence("GD_EDU_EXAM", 6090, ScoreScale.GAOKAO_750_TENTH),
                evidence("SZPU_ADMISSION", 6090, ScoreScale.GAOKAO_750_TENTH));

        assertThat(ConsistencyEvaluator.evaluate(currents, ScoreScale.GAOKAO_750_TENTH))
                .isEqualTo(DossierStatus.CONSISTENT);
    }

    @Test
    void unexpectedScaleYieldsScaleConflict() {
        List<FactEvidence> currents = List.of(
                evidence("GD_EDU_EXAM", 6090, ScoreScale.GAOKAO_750_TENTH),
                evidence("MANUAL", 6090, ScoreScale.ART_COMPOSITE_TENTH));

        assertThat(ConsistencyEvaluator.evaluate(currents, ScoreScale.GAOKAO_750_TENTH))
                .isEqualTo(DossierStatus.SCALE_CONFLICT);
    }

    @Test
    void noEvidenceYieldsNoEvidence() {
        assertThat(ConsistencyEvaluator.evaluate(List.of(), ScoreScale.GAOKAO_750_TENTH))
                .isEqualTo(DossierStatus.NO_EVIDENCE);
    }

    @Test
    void singleSourceYieldsSingleSource() {
        List<FactEvidence> currents = List.of(
                evidence("GD_EDU_EXAM", 6090, ScoreScale.GAOKAO_750_TENTH));

        assertThat(ConsistencyEvaluator.evaluate(currents, ScoreScale.GAOKAO_750_TENTH))
                .isEqualTo(DossierStatus.SINGLE_SOURCE);
    }
}

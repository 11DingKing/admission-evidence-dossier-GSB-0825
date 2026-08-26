package com.gsb.admission.dossier.domain;

import java.util.List;

public final class ConsistencyEvaluator {

    private ConsistencyEvaluator() {
    }

    public static DossierStatus evaluate(List<FactEvidence> currentValues, ScoreScale expectedScale) {
        if (currentValues.isEmpty()) {
            return DossierStatus.NO_EVIDENCE;
        }
        if (currentValues.stream().anyMatch(e -> e.scoreScale() != expectedScale)) {
            return DossierStatus.SCALE_CONFLICT;
        }
        if (currentValues.size() == 1) {
            return DossierStatus.SINGLE_SOURCE;
        }
        boolean sameScore = currentValues.stream()
                .mapToLong(FactEvidence::scoreTenths)
                .distinct()
                .count() == 1;
        return sameScore ? DossierStatus.CONSISTENT : DossierStatus.CONFLICT;
    }
}

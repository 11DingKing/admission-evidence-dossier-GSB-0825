package com.gsb.admission.dossier.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RevisionPolicyTest {

    private static final LocalDate EFFECTIVE = LocalDate.of(2025, 7, 1);
    private static final Instant AS_OF = Instant.parse("2025-07-26T00:00:00Z");

    private static FactEvidence revision(int revision, Instant recordedAt) {
        return new FactEvidence(
                "GD_EDU_EXAM",
                revision,
                6000L + revision,
                ScoreScale.GAOKAO_750_TENTH,
                EFFECTIVE,
                null,
                recordedAt);
    }

    @Test
    void lateOlderRevisionNeverReclaimsCurrent() {
        FactEvidence rev3 = revision(3, Instant.parse("2025-07-24T09:00:00Z"));
        List<FactEvidence> chain = new ArrayList<>(List.of(rev3));

        Optional<FactEvidence> current = RevisionPolicy.currentValue(chain, AS_OF);
        assertThat(current).contains(rev3);

        FactEvidence rev2 = revision(2, Instant.parse("2025-07-25T09:00:00Z"));
        chain.add(rev2);
        current = RevisionPolicy.currentValue(chain, AS_OF);
        assertThat(current).contains(rev3);
        assertThat(RevisionPolicy.isSuperseded(rev3, current)).isFalse();
        assertThat(RevisionPolicy.isSuperseded(rev2, current)).isTrue();

        FactEvidence rev4 = revision(4, Instant.parse("2025-07-25T18:00:00Z"));
        chain.add(rev4);
        current = RevisionPolicy.currentValue(chain, AS_OF);
        assertThat(current).contains(rev4);
        assertThat(RevisionPolicy.isSuperseded(rev4, current)).isFalse();
        assertThat(RevisionPolicy.isSuperseded(rev3, current)).isTrue();
        assertThat(RevisionPolicy.isSuperseded(rev2, current)).isTrue();
    }

    @Test
    void revisionRecordedAfterAsOfIsNotVisible() {
        FactEvidence rev1 = revision(1, Instant.parse("2025-07-20T09:00:00Z"));
        FactEvidence rev2 = revision(2, Instant.parse("2025-08-01T09:00:00Z"));

        Optional<FactEvidence> current =
                RevisionPolicy.currentValue(List.of(rev1, rev2), Instant.parse("2025-07-26T00:00:00Z"));

        assertThat(current).contains(rev1);
    }

    @Test
    void revisionNotYetEffectiveIsNotCurrent() {
        FactEvidence rev1 = revision(1, Instant.parse("2025-07-25T23:00:00Z"));
        FactEvidence rev2 = new FactEvidence(
                "GD_EDU_EXAM", 2, 6090, ScoreScale.GAOKAO_750_TENTH,
                LocalDate.of(2025, 8, 1), null,
                Instant.parse("2025-07-25T23:30:00Z"));

        Optional<FactEvidence> current =
                RevisionPolicy.currentValue(List.of(rev1, rev2), AS_OF);

        assertThat(current).contains(rev1);
    }
}

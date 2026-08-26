package com.dossier.admission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.DossierStatus;
import com.dossier.admission.domain.FactKey;
import com.dossier.admission.domain.SourceRecord;
import com.dossier.admission.repository.DossierEntryRepository;
import com.dossier.admission.repository.DossierRepository;
import com.dossier.admission.service.DossierService;
import com.dossier.admission.service.SourceService;
import com.dossier.admission.web.dto.BatchSourceResponse;
import com.dossier.admission.web.dto.SourceRecordRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DossierServiceTest {

    private static final String PROVINCE = "44";
    private static final int YEAR = 2025;
    private static final String SUBJECT = "PHYSICS";
    private static final String SCHOOL = "11113";
    private static final String GROUP = "001";
    private static final String SCALE = "PHYSICS_750_T1";

    @Autowired
    private DossierService dossierService;
    @Autowired
    private SourceService sourceService;
    @Autowired
    private DossierRepository dossierRepository;
    @Autowired
    private DossierEntryRepository dossierEntryRepository;

    private FactKey factKey() {
        return new FactKey(PROVINCE, YEAR, SUBJECT, SCHOOL, GROUP);
    }

    private SourceRecordRequest req(String sourceId, int revision, long score, String scale,
                                     Instant recordedAt) {
        return new SourceRecordRequest(
                PROVINCE, YEAR, SUBJECT, SCHOOL, GROUP,
                sourceId, revision, score, scale, String.valueOf(score / 10.0),
                Instant.parse("2025-07-10T00:00:00Z"), null, recordedAt,
                "DOC-" + sourceId, "原文 " + sourceId);
    }

    @Test
    void revisionOutOfOrder_oldRevisionStaysButDoesNotReclaimCurrent() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        Instant t1 = Instant.parse("2025-07-15T10:00:00Z");
        Instant t2 = t1.plus(1, ChronoUnit.DAYS);
        Instant t3 = t2.plus(1, ChronoUnit.DAYS);

        sourceService.submitBatch(List.of(
                req("A", 2, 6090L, SCALE, t2)
        ), factKey());

        sourceService.submitBatch(List.of(
                req("A", 1, 6100L, SCALE, t1)
        ), factKey());

        List<SourceRecord> active = sourceService.getActiveSources(factKey());
        assertThat(active).hasSize(1);
        assertThat(active.get(0).getSourceRevision()).isEqualTo(2);
        assertThat(active.get(0).getScoreValue()).isEqualTo(6090L);

        List<SourceRecord> all = sourceService.getAllSources(factKey());
        assertThat(all).hasSize(2);
    }

    @Test
    void factKeyMismatch_sourceRejectedForDossier() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        SourceRecordRequest wrong = new SourceRecordRequest(
                "42", YEAR, "ART", "99999", "999",
                "X", 1, 5000L, SCALE, "500.0",
                Instant.parse("2025-07-10T00:00:00Z"), null, Instant.now(),
                "DOC-X", "wrong fact key");

        BatchSourceResponse resp = sourceService.submitBatch(List.of(wrong), dossier.factKey());
        assertThat(resp.successCount()).isZero();
        assertThat(resp.failureCount()).isEqualTo(1);
        assertThat(resp.errors().get(0).errorCode()).isEqualTo("FACT_KEY_MISMATCH");
    }

    @Test
    void scaleConflict_markedWhenScaleDiffersFromExpected() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        sourceService.submitBatch(List.of(
                req("A", 1, 6090L, SCALE, Instant.now()),
                req("B", 1, 6090L, "OTHER_SCALE", Instant.now())
        ), factKey());

        var response = dossierService.getDossier(dossier.getDossierId());
        assertThat(response.status()).isEqualTo(DossierStatus.SCALE_CONFLICT);
    }

    @Test
    void asOf_sealCapturesOnlySourcesRecordedBeforeTimestamp() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        Instant before = Instant.parse("2025-07-15T10:00:00Z");
        Instant asOf = Instant.parse("2025-07-20T10:00:00Z");
        Instant after = Instant.parse("2025-07-25T10:00:00Z");

        sourceService.submitBatch(List.of(
                req("A", 1, 6090L, SCALE, before)
        ), factKey());

        sourceService.submitBatch(List.of(
                req("B", 1, 6090L, SCALE, after)
        ), factKey());

        dossierService.sealDossier(dossier.getDossierId(), asOf);

        var sealed = dossierService.getDossier(dossier.getDossierId());
        assertThat(sealed.sealedEntries()).hasSize(1);
        assertThat(sealed.sealedEntries().get(0).sourceId()).isEqualTo("A");
    }

    @Test
    void sealedDossier_isImmutable() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        sourceService.submitBatch(List.of(
                req("A", 1, 6090L, SCALE, Instant.now())
        ), factKey());

        dossierService.sealDossier(dossier.getDossierId(), Instant.now());

        assertThatThrownBy(() -> dossierService.sealDossier(dossier.getDossierId(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已封存");

        Dossier reloaded = dossierRepository.findById(dossier.getDossierId()).orElseThrow();
        assertThat(reloaded.isSealed()).isTrue();

        var entries = dossierEntryRepository.findByDossierIdOrderBySourceId(dossier.getDossierId());
        assertThat(entries).hasSize(1);
    }

    @Test
    void partialFailure_someSourcesSucceedOthersFail() {
        Dossier dossier = dossierService.createDossier(factKey(), SCALE);

        SourceRecordRequest good = req("A", 1, 6090L, SCALE, Instant.now());
        SourceRecordRequest duplicate = req("A", 1, 6090L, SCALE, Instant.now());
        SourceRecordRequest wrongKey = new SourceRecordRequest(
                "42", YEAR, "ART", "99999", "999",
                "X", 1, 5000L, SCALE, "500.0",
                Instant.parse("2025-07-10T00:00:00Z"), null, Instant.now(),
                "DOC-X", "wrong");

        BatchSourceResponse first = sourceService.submitBatch(List.of(good), factKey());
        assertThat(first.successCount()).isEqualTo(1);

        BatchSourceResponse second = sourceService.submitBatch(
                List.of(duplicate, wrongKey, req("B", 1, 6090L, SCALE, Instant.now())),
                factKey());

        assertThat(second.successCount()).isEqualTo(1);
        assertThat(second.failureCount()).isEqualTo(2);
        assertThat(second.partialFailure()).isTrue();
        assertThat(second.errors()).extracting("errorCode")
                .containsExactlyInAnyOrder("DUPLICATE_REVISION", "FACT_KEY_MISMATCH");
    }

    @Test
    void shenzhenScenario_conflictThenConsistent() {
        FactKey szKey = new FactKey("44", 2025, "PHYSICS", "11113", "001");
        Dossier dossier = dossierService.createDossier(szKey, "PHYSICS_750_T1");

        Instant t1 = Instant.parse("2025-07-15T10:00:00Z");
        sourceService.submitBatch(List.of(
                new SourceRecordRequest("44", 2025, "PHYSICS", "11113", "001",
                        "SOURCE_A", 1, 6100L, "PHYSICS_750_T1", "610.0",
                        Instant.parse("2025-07-10T00:00:00Z"), null, t1,
                        "粤招办[2025]12号", "A rev1 610.0"),
                new SourceRecordRequest("44", 2025, "PHYSICS", "11113", "001",
                        "SOURCE_B", 1, 6090L, "PHYSICS_750_T1", "609.0",
                        Instant.parse("2025-07-10T00:00:00Z"), null, t1.plus(2, ChronoUnit.HOURS),
                        "粤招办[2025]13号", "B rev1 609.0")
        ), szKey);

        var conflict = dossierService.getDossier(dossier.getDossierId());
        assertThat(conflict.status()).isEqualTo(DossierStatus.CONFLICT);

        sourceService.submitBatch(List.of(
                new SourceRecordRequest("44", 2025, "PHYSICS", "11113", "001",
                        "SOURCE_A", 2, 6090L, "PHYSICS_750_T1", "609.0",
                        Instant.parse("2025-07-10T00:00:00Z"), null, t1.plus(1, ChronoUnit.DAYS),
                        "粤招办[2025]12号-勘误", "A rev2 609.0")
        ), szKey);

        var consistent = dossierService.getDossier(dossier.getDossierId());
        assertThat(consistent.status()).isEqualTo(DossierStatus.CONSISTENT);
        assertThat(consistent.sources()).hasSize(2);
        assertThat(consistent.sources()).allMatch(s -> s.scoreValue() == 6090L);
    }

    @Test
    void wuhanScenario_doesNotAttachToShenzhenDossier() {
        FactKey szKey = new FactKey("44", 2025, "PHYSICS", "11113", "001");
        Dossier szDossier = dossierService.createDossier(szKey, "PHYSICS_750_T1");

        SourceRecordRequest wuhanSource = new SourceRecordRequest(
                "42", 2025, "ART", "13796", "018",
                "SOURCE_C", 1, 6044L, "ART_COMPREHENSIVE_T1", "604.4",
                Instant.parse("2025-07-11T00:00:00Z"), null,
                Instant.parse("2025-07-16T10:00:00Z"),
                "鄂招办[2025]21号", "武职 数字媒体艺术 604.4");

        BatchSourceResponse resp = sourceService.submitBatch(List.of(wuhanSource), szDossier.factKey());
        assertThat(resp.successCount()).isZero();
        assertThat(resp.errors().get(0).errorCode()).isEqualTo("FACT_KEY_MISMATCH");

        var szView = dossierService.getDossier(szDossier.getDossierId());
        assertThat(szView.sources()).isEmpty();
    }
}

package com.gsb.admission.dossier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gsb.admission.dossier.domain.DossierStatus;
import com.gsb.admission.dossier.domain.FactKey;
import com.gsb.admission.dossier.domain.FetchOutcome;
import com.gsb.admission.dossier.domain.LoadState;
import com.gsb.admission.dossier.domain.Origin;
import com.gsb.admission.dossier.domain.ScoreScale;
import com.gsb.admission.dossier.domain.SubjectCategory;
import com.gsb.admission.dossier.model.DossierEntity;
import com.gsb.admission.dossier.model.DossierSealEntity;
import com.gsb.admission.dossier.model.DossierSourceEntity;
import com.gsb.admission.dossier.model.SourceRecordEntity;
import com.gsb.admission.dossier.model.SourceRegistryEntity;
import com.gsb.admission.dossier.repo.DossierRepository;
import com.gsb.admission.dossier.repo.DossierSealRepository;
import com.gsb.admission.dossier.repo.DossierSourceRepository;
import com.gsb.admission.dossier.repo.SourceRecordRepository;
import com.gsb.admission.dossier.repo.SourceRegistryRepository;
import com.gsb.admission.dossier.web.dto.DossierDetailView;
import com.gsb.admission.dossier.web.dto.FetchReportView;
import com.gsb.admission.dossier.web.dto.FetchResultView;
import com.gsb.admission.dossier.web.dto.ManualSourceRecordRequest;
import com.gsb.admission.dossier.web.dto.SealView;
import com.gsb.admission.dossier.web.dto.SourceRecordView;
import com.gsb.admission.dossier.web.dto.SourceStateView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DossierServiceTest {

    private static final UUID SHENZHEN = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final Instant T1 = Instant.parse("2025-07-20T12:00:00Z");
    private static final Instant T2 = Instant.parse("2025-07-26T00:00:00Z");

    private static SourcePublicationPayload publication(String sourceId, String province, int year,
                                                        SubjectCategory category, String school, String group,
                                                        int revision, long tenths, ScoreScale scale,
                                                        String docNo, LocalDate effectiveFrom, Instant recordedAt) {
        return new SourcePublicationPayload(sourceId, province, year, category, school, group,
                revision, tenths, scale, docNo, effectiveFrom, null, recordedAt,
                "公告原文 " + sourceId + " rev" + revision);
    }

    private static final SourcePublicationPayload GD_REV1 = publication(
            "GD_EDU_EXAM", "44", 2025, SubjectCategory.PHYSICS, "11113", "203",
            1, 6100, ScoreScale.GAOKAO_750_TENTH,
            "粤教考函〔2025〕37号", LocalDate.of(2025, 7, 19), Instant.parse("2025-07-19T09:00:00Z"));
    private static final SourcePublicationPayload GD_REV2 = publication(
            "GD_EDU_EXAM", "44", 2025, SubjectCategory.PHYSICS, "11113", "203",
            2, 6090, ScoreScale.GAOKAO_750_TENTH,
            "粤教考函〔2025〕41号", LocalDate.of(2025, 7, 25), Instant.parse("2025-07-25T10:00:00Z"));
    private static final SourcePublicationPayload SZPU_REV1 = publication(
            "SZPU_ADMISSION", "44", 2025, SubjectCategory.PHYSICS, "11113", "203",
            1, 6090, ScoreScale.GAOKAO_750_TENTH,
            "深职大招〔2025〕18号", LocalDate.of(2025, 7, 20), Instant.parse("2025-07-20T08:30:00Z"));
    private static final SourcePublicationPayload CHSI_WUHAN = publication(
            "CHSI_GGKG", "42", 2025, SubjectCategory.ART_COMPOSITE, "10834", "Y01",
            1, 6044, ScoreScale.ART_COMPOSITE_TENTH,
            "GGKG-2025-HB-ART-0612", LocalDate.of(2025, 7, 22), Instant.parse("2025-07-22T12:00:00Z"));

    private final DossierRepository dossierRepository = mock(DossierRepository.class);
    private final DossierSourceRepository dossierSourceRepository = mock(DossierSourceRepository.class);
    private final SourceRecordRepository sourceRecordRepository = mock(SourceRecordRepository.class);
    private final DossierSealRepository dossierSealRepository = mock(DossierSealRepository.class);
    private final SourceRegistryRepository sourceRegistryRepository = mock(SourceRegistryRepository.class);
    private final FakeSourceAdapter adapter = new FakeSourceAdapter();

    private final List<SourceRecordEntity> records = new ArrayList<>();
    private final List<DossierSealEntity> seals = new ArrayList<>();
    private final List<DossierSourceEntity> bindings = new ArrayList<>();

    private DossierService service;

    @BeforeEach
    void setUp() {
        DossierEntity shenzhen = dossier(SHENZHEN, "44", SubjectCategory.PHYSICS,
                "11113", "203", ScoreScale.GAOKAO_750_TENTH);
        when(dossierRepository.findById(any(UUID.class))).thenAnswer(inv -> {
            UUID id = inv.getArgument(0);
            return SHENZHEN.equals(id) ? Optional.of(shenzhen) : Optional.empty();
        });

        bindings.add(binding(SHENZHEN, "GD_EDU_EXAM", 0));
        bindings.add(binding(SHENZHEN, "SZPU_ADMISSION", 1));
        bindings.add(binding(SHENZHEN, "CHSI_GGKG", 2));
        when(dossierSourceRepository.findByDossierIdOrderByPositionAsc(any(UUID.class)))
                .thenAnswer(inv -> bindings.stream()
                        .filter(b -> b.getDossierId().equals(inv.getArgument(0)))
                        .sorted(Comparator.comparingInt(DossierSourceEntity::getPosition))
                        .toList());
        when(dossierSourceRepository.findByDossierIdAndSourceId(any(UUID.class), anyString()))
                .thenAnswer(inv -> bindings.stream()
                        .filter(b -> b.getDossierId().equals(inv.getArgument(0))
                                && b.getSourceId().equals(inv.getArgument(1)))
                        .findFirst());
        when(dossierSourceRepository.save(any(DossierSourceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(sourceRecordRepository.findByDossierIdOrderByRecordedAtAsc(any(UUID.class)))
                .thenAnswer(inv -> records.stream()
                        .filter(r -> r.getDossierId().equals(inv.getArgument(0)))
                        .sorted(Comparator.comparing(SourceRecordEntity::getRecordedAt))
                        .toList());
        when(sourceRecordRepository.findByDossierIdAndSourceIdOrderBySourceRevisionAsc(any(UUID.class), anyString()))
                .thenAnswer(inv -> records.stream()
                        .filter(r -> r.getDossierId().equals(inv.getArgument(0))
                                && r.getSourceId().equals(inv.getArgument(1)))
                        .sorted(Comparator.comparingInt(SourceRecordEntity::getSourceRevision))
                        .toList());
        when(sourceRecordRepository.findByDossierIdAndSourceIdAndSourceRevision(any(UUID.class), anyString(), anyInt()))
                .thenAnswer(inv -> records.stream()
                        .filter(r -> r.getDossierId().equals(inv.getArgument(0))
                                && r.getSourceId().equals(inv.getArgument(1))
                                && r.getSourceRevision() == (int) inv.getArgument(2))
                        .findFirst());
        when(sourceRecordRepository.existsByDossierIdAndSourceIdAndSourceRevision(any(UUID.class), anyString(), anyInt()))
                .thenAnswer(inv -> records.stream().anyMatch(r -> r.getDossierId().equals(inv.getArgument(0))
                        && r.getSourceId().equals(inv.getArgument(1))
                        && r.getSourceRevision() == (int) inv.getArgument(2)));
        when(sourceRecordRepository.existsByDossierIdAndRecordedAtAfter(any(UUID.class), any(Instant.class)))
                .thenAnswer(inv -> records.stream().anyMatch(r -> r.getDossierId().equals(inv.getArgument(0))
                        && r.getRecordedAt().isAfter(inv.getArgument(1))));
        when(sourceRecordRepository.save(any(SourceRecordEntity.class))).thenAnswer(inv -> {
            SourceRecordEntity entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            records.add(entity);
            return entity;
        });

        when(dossierSealRepository.findByDossierIdOrderBySealedAtAsc(any(UUID.class)))
                .thenAnswer(inv -> seals.stream()
                        .filter(s -> s.getDossierId().equals(inv.getArgument(0)))
                        .toList());
        when(dossierSealRepository.findBySealIdAndDossierId(any(UUID.class), any(UUID.class)))
                .thenAnswer(inv -> seals.stream()
                        .filter(s -> s.getSealId().equals(inv.getArgument(0))
                                && s.getDossierId().equals(inv.getArgument(1)))
                        .findFirst());
        when(dossierSealRepository.existsByDossierIdAndAsOf(any(UUID.class), any(Instant.class)))
                .thenAnswer(inv -> seals.stream().anyMatch(s -> s.getDossierId().equals(inv.getArgument(0))
                        && s.getAsOf().equals(inv.getArgument(1))));
        when(dossierSealRepository.save(any(DossierSealEntity.class))).thenAnswer(inv -> {
            seals.add(inv.getArgument(0));
            return inv.getArgument(0);
        });

        when(sourceRegistryRepository.findAllBySourceIdIn(anyCollection())).thenAnswer(inv -> {
            Collection<String> ids = inv.getArgument(0);
            return ids.stream().map(id -> {
                SourceRegistryEntity entity = new SourceRegistryEntity();
                entity.setSourceId(id);
                entity.setDisplayName("名称-" + id);
                entity.setProvider("PUBLICATION_TABLE");
                return entity;
            }).toList();
        });

        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        service = new DossierService(dossierRepository, dossierSourceRepository,
                sourceRecordRepository, dossierSealRepository, sourceRegistryRepository,
                adapter, objectMapper);
    }

    private static DossierEntity dossier(UUID id, String province, SubjectCategory category,
                                         String school, String group, ScoreScale scale) {
        DossierEntity entity = new DossierEntity();
        entity.setDossierId(id);
        entity.setProvinceCode(province);
        entity.setAdmissionYear(2025);
        entity.setSubjectCategory(category);
        entity.setSchoolCode(school);
        entity.setMajorGroupCode(group);
        entity.setExpectedScale(scale);
        entity.setCreatedAt(Instant.parse("2025-07-01T00:00:00Z"));
        return entity;
    }

    private static DossierSourceEntity binding(UUID dossierId, String sourceId, int position) {
        DossierSourceEntity entity = new DossierSourceEntity();
        entity.setDossierId(dossierId);
        entity.setSourceId(sourceId);
        entity.setPosition(position);
        entity.setLoadState(LoadState.MISSING);
        return entity;
    }

    private static FetchResultView result(FetchReportView report, String sourceId) {
        return report.results().stream()
                .filter(r -> r.sourceId().equals(sourceId))
                .findFirst()
                .orElseThrow();
    }

    private static SourceStateView state(DossierDetailView detail, String sourceId) {
        return detail.sources().stream()
                .filter(s -> s.sourceId().equals(sourceId))
                .findFirst()
                .orElseThrow();
    }

    private static SourceRecordView chainRecord(DossierDetailView detail, String sourceId, int revision) {
        return detail.evidenceChain().stream()
                .filter(r -> r.sourceId().equals(sourceId) && r.sourceRevision() == revision)
                .findFirst()
                .orElseThrow();
    }

    @Test
    void asOfReplay_conflictAtT1_thenConsistentAtT2WithSupersededRevision() {
        adapter.publish(GD_REV1, GD_REV2, SZPU_REV1, CHSI_WUHAN);

        FetchReportView reportAtT1 = service.fetchSources(SHENZHEN, T1);

        assertThat(reportAtT1.dossierStatus()).isEqualTo(DossierStatus.CONFLICT);
        assertThat(result(reportAtT1, "GD_EDU_EXAM").outcome()).isEqualTo(FetchOutcome.LOADED);
        assertThat(result(reportAtT1, "GD_EDU_EXAM").record().scoreTenths()).isEqualTo(6100);
        assertThat(result(reportAtT1, "SZPU_ADMISSION").outcome()).isEqualTo(FetchOutcome.LOADED);
        assertThat(result(reportAtT1, "SZPU_ADMISSION").record().scoreTenths()).isEqualTo(6090);
        assertThat(result(reportAtT1, "CHSI_GGKG").outcome()).isEqualTo(FetchOutcome.ERROR);
        assertThat(result(reportAtT1, "CHSI_GGKG").errorCode()).isEqualTo("NO_PUBLICATION_AS_OF");
        assertThat(result(reportAtT1, "CHSI_GGKG").record()).isNull();

        DossierDetailView viewAtT1 = service.getDossier(SHENZHEN, T1);
        assertThat(viewAtT1.status()).isEqualTo(DossierStatus.CONFLICT);
        assertThat(state(viewAtT1, "GD_EDU_EXAM").current().sourceRevision()).isEqualTo(1);
        assertThat(state(viewAtT1, "GD_EDU_EXAM").current().scoreTenths()).isEqualTo(6100);
        assertThat(state(viewAtT1, "SZPU_ADMISSION").current().scoreTenths()).isEqualTo(6090);
        assertThat(state(viewAtT1, "CHSI_GGKG").current()).isNull();

        FetchReportView reportAtT2 = service.fetchSources(SHENZHEN, T2);

        assertThat(result(reportAtT2, "GD_EDU_EXAM").outcome()).isEqualTo(FetchOutcome.LOADED);
        assertThat(result(reportAtT2, "GD_EDU_EXAM").record().sourceRevision()).isEqualTo(2);
        assertThat(result(reportAtT2, "SZPU_ADMISSION").outcome()).isEqualTo(FetchOutcome.LOADED);
        assertThat(result(reportAtT2, "CHSI_GGKG").outcome()).isEqualTo(FetchOutcome.ERROR);
        assertThat(result(reportAtT2, "CHSI_GGKG").errorCode()).isEqualTo("FACT_KEY_MISMATCH");
        assertThat(reportAtT2.dossierStatus()).isEqualTo(DossierStatus.CONSISTENT);

        DossierDetailView viewAtT2 = service.getDossier(SHENZHEN, T2);
        assertThat(viewAtT2.status()).isEqualTo(DossierStatus.CONSISTENT);
        assertThat(state(viewAtT2, "GD_EDU_EXAM").current().sourceRevision()).isEqualTo(2);
        assertThat(state(viewAtT2, "GD_EDU_EXAM").current().scoreTenths()).isEqualTo(6090);
        assertThat(viewAtT2.evidenceChain()).hasSize(3);
        assertThat(chainRecord(viewAtT2, "GD_EDU_EXAM", 1).superseded()).isTrue();
        assertThat(chainRecord(viewAtT2, "GD_EDU_EXAM", 2).superseded()).isFalse();
        assertThat(chainRecord(viewAtT2, "SZPU_ADMISSION", 1).superseded()).isFalse();

        DossierDetailView replayedT1 = service.getDossier(SHENZHEN, T1);
        assertThat(replayedT1.status()).isEqualTo(DossierStatus.CONFLICT);
        assertThat(state(replayedT1, "GD_EDU_EXAM").current().sourceRevision()).isEqualTo(1);
        assertThat(state(replayedT1, "GD_EDU_EXAM").current().scoreTenths()).isEqualTo(6100);
        assertThat(chainRecord(replayedT1, "GD_EDU_EXAM", 1).superseded()).isFalse();
        assertThat(records).hasSize(3);
    }

    @Test
    void factKeyMismatchIsRejectedAndNeverPersisted() {
        adapter.publish(CHSI_WUHAN);

        FetchReportView report = service.fetchSources(SHENZHEN, T2);

        FetchResultView chsi = result(report, "CHSI_GGKG");
        assertThat(chsi.outcome()).isEqualTo(FetchOutcome.ERROR);
        assertThat(chsi.errorCode()).isEqualTo("FACT_KEY_MISMATCH");
        assertThat(chsi.record()).isNull();
        assertThat(result(report, "GD_EDU_EXAM").outcome()).isEqualTo(FetchOutcome.ERROR);
        assertThat(result(report, "GD_EDU_EXAM").errorCode()).isEqualTo("NO_PUBLICATION_AS_OF");
        verify(sourceRecordRepository, never()).save(any());
        assertThat(records).isEmpty();
        assertThat(report.dossierStatus()).isEqualTo(DossierStatus.NO_EVIDENCE);
    }

    @Test
    void manualRecordWithWrongScaleIsSavedAndMarksScaleConflict() {
        ManualSourceRecordRequest request = new ManualSourceRecordRequest(
                "GD_EDU_EXAM", 1, 6044, ScoreScale.ART_COMPOSITE_TENTH,
                "人工录入-01", LocalDate.of(2025, 7, 20), null,
                Instant.parse("2025-07-21T00:00:00Z"),
                "人工录入：艺术综合分证据");

        SourceRecordView saved = service.addSourceRecord(SHENZHEN, request);

        assertThat(saved.origin()).isEqualTo(Origin.MANUAL);
        assertThat(saved.scoreScale()).isEqualTo(ScoreScale.ART_COMPOSITE_TENTH);
        assertThat(saved.scoreTenths()).isEqualTo(6044);
        assertThat(saved.scoreDisplay()).isEqualTo("604.4");
        assertThat(saved.superseded()).isFalse();
        assertThat(records).hasSize(1);

        DossierDetailView detail = service.getDossier(SHENZHEN, T2);
        assertThat(detail.status()).isEqualTo(DossierStatus.SCALE_CONFLICT);
        assertThat(state(detail, "GD_EDU_EXAM").current().contentHash()).startsWith("sha256:");
    }

    @Test
    void oneUnavailableSourceFailsButOthersLoadAndChainStaysIntact() {
        adapter.publish(GD_REV1, SZPU_REV1);
        adapter.makeUnavailable("CHSI_GGKG", "SOURCE_UNAVAILABLE");

        FetchReportView report = service.fetchSources(SHENZHEN, T1);

        assertThat(result(report, "GD_EDU_EXAM").outcome()).isEqualTo(FetchOutcome.LOADED);
        assertThat(result(report, "SZPU_ADMISSION").outcome()).isEqualTo(FetchOutcome.LOADED);
        FetchResultView failed = result(report, "CHSI_GGKG");
        assertThat(failed.outcome()).isEqualTo(FetchOutcome.ERROR);
        assertThat(failed.errorCode()).isEqualTo("SOURCE_UNAVAILABLE");
        assertThat(failed.record()).isNull();

        DossierDetailView detail = service.getDossier(SHENZHEN, T1);
        assertThat(detail.evidenceChain()).hasSize(2);
        SourceStateView chsiState = state(detail, "CHSI_GGKG");
        assertThat(chsiState.loadState()).isEqualTo(LoadState.ERROR);
        assertThat(chsiState.errorCode()).isEqualTo("SOURCE_UNAVAILABLE");
        assertThat(chsiState.current()).isNull();
        assertThat(state(detail, "GD_EDU_EXAM").loadState()).isEqualTo(LoadState.OK);
    }

    @Test
    void sealedSnapshotIsImmutableAndFlagsNewerEvidence() {
        adapter.publish(GD_REV1, GD_REV2, SZPU_REV1, CHSI_WUHAN);
        service.fetchSources(SHENZHEN, T1);

        SealView seal = service.createSeal(SHENZHEN, T1);
        assertThat(seal.status()).isEqualTo(DossierStatus.CONFLICT);
        assertThat(seal.snapshot().get("status")).isEqualTo("CONFLICT");
        assertThat(seal.hasNewerEvidence()).isFalse();
        String contentHash = seal.contentHash();
        UUID sealId = seal.sealId();

        service.fetchSources(SHENZHEN, T2);

        SealView reread = service.getSeal(SHENZHEN, sealId);
        assertThat(reread.contentHash()).isEqualTo(contentHash);
        assertThat(reread.status()).isEqualTo(DossierStatus.CONFLICT);
        assertThat(reread.snapshot().get("status")).isEqualTo("CONFLICT");
        assertThat(reread.hasNewerEvidence()).isTrue();

        ApiException conflict = catchThrowableOfType(
                ApiException.class, () -> service.createSeal(SHENZHEN, T1));
        assertThat(conflict.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflict.getErrorCode()).isEqualTo("SEAL_CONFLICT");
    }

    private static final class FakeSourceAdapter implements SourceAdapter {

        private final Map<String, List<SourcePublicationPayload>> publications = new HashMap<>();
        private final Map<String, SourceUnavailableException> unavailable = new HashMap<>();

        void publish(SourcePublicationPayload... payloads) {
            for (SourcePublicationPayload payload : payloads) {
                publications.computeIfAbsent(payload.sourceId(), key -> new ArrayList<>()).add(payload);
            }
        }

        void makeUnavailable(String sourceId, String errorCode) {
            unavailable.put(sourceId, new SourceUnavailableException(errorCode, "source unavailable: " + errorCode));
        }

        @Override
        public Optional<SourcePublicationPayload> fetch(String sourceId, FactKey factKey, Instant asOf) {
            SourceUnavailableException failure = unavailable.get(sourceId);
            if (failure != null) {
                throw failure;
            }
            List<SourcePublicationPayload> list = publications.get(sourceId);
            if (list == null) {
                return Optional.empty();
            }
            return list.stream()
                    .filter(p -> p.admissionYear() == factKey.admissionYear())
                    .filter(p -> !p.recordedAt().isAfter(asOf))
                    .max(Comparator.comparingInt(SourcePublicationPayload::sourceRevision));
        }
    }
}

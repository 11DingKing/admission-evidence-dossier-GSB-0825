package com.gsb.admission.dossier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gsb.admission.dossier.domain.ConsistencyEvaluator;
import com.gsb.admission.dossier.domain.DossierStatus;
import com.gsb.admission.dossier.domain.FactEvidence;
import com.gsb.admission.dossier.domain.FactKey;
import com.gsb.admission.dossier.domain.FetchOutcome;
import com.gsb.admission.dossier.domain.FreshnessPolicy;
import com.gsb.admission.dossier.domain.HashService;
import com.gsb.admission.dossier.domain.LoadState;
import com.gsb.admission.dossier.domain.Origin;
import com.gsb.admission.dossier.domain.RevisionPolicy;
import com.gsb.admission.dossier.domain.Scores;
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
import com.gsb.admission.dossier.web.dto.CreateDossierRequest;
import com.gsb.admission.dossier.web.dto.DossierDetailView;
import com.gsb.admission.dossier.web.dto.DossierSummaryView;
import com.gsb.admission.dossier.web.dto.FactKeyView;
import com.gsb.admission.dossier.web.dto.FetchReportView;
import com.gsb.admission.dossier.web.dto.FetchResultView;
import com.gsb.admission.dossier.web.dto.ManualSourceRecordRequest;
import com.gsb.admission.dossier.web.dto.RevisionChainView;
import com.gsb.admission.dossier.web.dto.RevisionView;
import com.gsb.admission.dossier.web.dto.SealSummaryView;
import com.gsb.admission.dossier.web.dto.SealView;
import com.gsb.admission.dossier.web.dto.SnapshotView;
import com.gsb.admission.dossier.web.dto.SourceRecordView;
import com.gsb.admission.dossier.web.dto.SourceStateView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DossierService {

    private final DossierRepository dossierRepository;
    private final DossierSourceRepository dossierSourceRepository;
    private final SourceRecordRepository sourceRecordRepository;
    private final DossierSealRepository dossierSealRepository;
    private final SourceRegistryRepository sourceRegistryRepository;
    private final SourceAdapter sourceAdapter;
    private final HashService hashService = new HashService();
    private final ObjectMapper sealMapper;

    public DossierService(DossierRepository dossierRepository,
                          DossierSourceRepository dossierSourceRepository,
                          SourceRecordRepository sourceRecordRepository,
                          DossierSealRepository dossierSealRepository,
                          SourceRegistryRepository sourceRegistryRepository,
                          SourceAdapter sourceAdapter,
                          ObjectMapper objectMapper) {
        this.dossierRepository = dossierRepository;
        this.dossierSourceRepository = dossierSourceRepository;
        this.sourceRecordRepository = sourceRecordRepository;
        this.dossierSealRepository = dossierSealRepository;
        this.sourceRegistryRepository = sourceRegistryRepository;
        this.sourceAdapter = sourceAdapter;
        this.sealMapper = objectMapper.copy().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    @Transactional(readOnly = true)
    public List<DossierSummaryView> listDossiers() {
        return dossierRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(d -> {
                    DossierView view = buildView(d, Instant.now(), true);
                    int sourceCount = dossierSourceRepository.findByDossierIdOrderByPositionAsc(d.getDossierId()).size();
                    return new DossierSummaryView(
                            d.getDossierId().toString(),
                            d.getProvinceCode(),
                            d.getAdmissionYear(),
                            d.getSubjectCategory(),
                            d.getSchoolCode(),
                            d.getMajorGroupCode(),
                            d.getExpectedScale(),
                            view.status(),
                            sourceCount,
                            d.getCreatedAt());
                })
                .toList();
    }

    @Transactional
    public DossierDetailView createDossier(CreateDossierRequest request) {
        FactKeyView fkv = request.factKey();
        FactKey factKey = new FactKey(fkv.provinceCode(), fkv.admissionYear(), fkv.subjectCategory(),
                fkv.schoolCode(), fkv.majorGroupCode());
        dossierRepository
                .findByProvinceCodeAndAdmissionYearAndSubjectCategoryAndSchoolCodeAndMajorGroupCode(
                        factKey.provinceCode(), factKey.admissionYear(), factKey.subjectCategory(),
                        factKey.schoolCode(), factKey.majorGroupCode())
                .ifPresent(existing -> {
                    throw ApiException.dossierExists();
                });

        List<String> requestedIds = request.sourceIds();
        List<String> sourceIds = requestedIds.stream().distinct().toList();
        if (sourceIds.size() != requestedIds.size()) {
            throw ApiException.validation("sourceIds contains duplicates");
        }
        List<SourceRegistryEntity> registered = sourceRegistryRepository.findAllBySourceIdIn(sourceIds);
        if (registered.size() != sourceIds.size()) {
            throw ApiException.validation("sourceIds contains unknown source id");
        }

        DossierEntity dossier = new DossierEntity();
        dossier.setProvinceCode(factKey.provinceCode());
        dossier.setAdmissionYear(factKey.admissionYear());
        dossier.setSubjectCategory(factKey.subjectCategory());
        dossier.setSchoolCode(factKey.schoolCode());
        dossier.setMajorGroupCode(factKey.majorGroupCode());
        dossier.setExpectedScale(factKey.expectedScale());
        dossier.setCreatedAt(Instant.now());
        dossier = dossierRepository.save(dossier);

        for (int i = 0; i < sourceIds.size(); i++) {
            DossierSourceEntity binding = new DossierSourceEntity();
            binding.setDossierId(dossier.getDossierId());
            binding.setSourceId(sourceIds.get(i));
            binding.setPosition(i);
            binding.setLoadState(LoadState.MISSING);
            dossierSourceRepository.save(binding);
        }
        return getDossier(dossier.getDossierId(), null);
    }

    @Transactional(readOnly = true)
    public DossierDetailView getDossier(UUID dossierId, Instant requestedAsOf) {
        DossierEntity dossier = getDossierOrThrow(dossierId);
        Instant asOf = requestedAsOf != null ? requestedAsOf : Instant.now();
        DossierView view = buildView(dossier, asOf, true);
        List<SealSummaryView> seals = dossierSealRepository
                .findByDossierIdOrderBySealedAtAsc(dossierId).stream()
                .map(this::toSealSummary)
                .toList();
        return new DossierDetailView(
                dossier.getDossierId().toString(),
                factKeyViewOf(dossier),
                hashService.factKeyHash(factKeyOf(dossier)),
                dossier.getExpectedScale(),
                view.status(),
                asOf,
                view.sources(),
                view.evidenceChain(),
                seals);
    }

    @Transactional
    public FetchReportView fetchSources(UUID dossierId, Instant requestedAsOf) {
        DossierEntity dossier = getDossierOrThrow(dossierId);
        FactKey factKey = factKeyOf(dossier);
        Instant asOf = requestedAsOf != null ? requestedAsOf : Instant.now();

        List<DossierSourceEntity> bindings = dossierSourceRepository
                .findByDossierIdOrderByPositionAsc(dossierId);
        Map<String, String> names = loadNames(bindings.stream()
                .map(DossierSourceEntity::getSourceId)
                .toList());

        List<FetchResultView> results = new ArrayList<>();
        for (DossierSourceEntity binding : bindings) {
            String sourceId = binding.getSourceId();
            Optional<SourcePublicationPayload> payloadOpt;
            try {
                payloadOpt = sourceAdapter.fetch(sourceId, factKey, asOf);
            } catch (SourceUnavailableException e) {
                markBinding(binding, LoadState.ERROR, e.getErrorCode(), e.getMessage());
                results.add(new FetchResultView(sourceId, FetchOutcome.ERROR, null,
                        e.getErrorCode(), e.getMessage()));
                continue;
            }

            if (payloadOpt.isEmpty()) {
                String code = "NO_PUBLICATION_AS_OF";
                String message = "no publication visible for source " + sourceId + " as of " + asOf;
                markBinding(binding, LoadState.ERROR, code, message);
                results.add(new FetchResultView(sourceId, FetchOutcome.ERROR, null, code, message));
                continue;
            }

            SourcePublicationPayload payload = payloadOpt.get();
            if (!payload.factKey().sameFactAs(factKey)) {
                String code = "FACT_KEY_MISMATCH";
                String message = "publication fact key does not match dossier fact key";
                markBinding(binding, LoadState.ERROR, code, message);
                results.add(new FetchResultView(sourceId, FetchOutcome.ERROR, null, code, message));
                continue;
            }

            SourceRecordEntity record = sourceRecordRepository
                    .findByDossierIdAndSourceIdAndSourceRevision(dossierId, sourceId, payload.sourceRevision())
                    .orElseGet(() -> sourceRecordRepository.save(newFetchedRecord(dossierId, payload)));
            markBinding(binding, LoadState.OK, null, null);

            List<FactEvidence> evidences = sourceRecordRepository
                    .findByDossierIdAndSourceIdOrderBySourceRevisionAsc(dossierId, sourceId).stream()
                    .map(DossierService::toEvidence)
                    .toList();
            Optional<FactEvidence> current = RevisionPolicy.currentValue(evidences, asOf);
            boolean isCurrent = current.isPresent() && current.get().sourceRevision() == record.getSourceRevision();
            SourceRecordView recordView = toRecordView(record, names.get(sourceId),
                    RevisionPolicy.isSuperseded(toEvidence(record), current), asOf);
            results.add(new FetchResultView(sourceId,
                    isCurrent ? FetchOutcome.LOADED : FetchOutcome.SUPERSEDED,
                    recordView, null, null));
        }

        DossierStatus status = buildView(dossier, asOf, true).status();
        return new FetchReportView(dossierId.toString(), asOf, status, results);
    }

    @Transactional
    public SourceRecordView addSourceRecord(UUID dossierId, ManualSourceRecordRequest request) {
        DossierEntity dossier = getDossierOrThrow(dossierId);
        dossierSourceRepository.findByDossierIdAndSourceId(dossierId, request.sourceId())
                .orElseThrow(() -> ApiException.validation(
                        "source not bound to dossier: " + request.sourceId()));
        if (sourceRecordRepository.existsByDossierIdAndSourceIdAndSourceRevision(
                dossierId, request.sourceId(), request.sourceRevision())) {
            throw ApiException.validation("source revision already exists in evidence chain: rev "
                    + request.sourceRevision());
        }

        Instant recordedAt = request.recordedAt() != null ? request.recordedAt() : Instant.now();
        SourceRecordEntity record = new SourceRecordEntity();
        record.setDossierId(dossierId);
        record.setSourceId(request.sourceId());
        record.setSourceRevision(request.sourceRevision());
        record.setScoreTenths(request.scoreTenths());
        record.setScoreScale(request.scoreScale());
        record.setSourceDocNo(request.sourceDocNo());
        record.setEffectiveFrom(request.effectiveFrom());
        record.setEffectiveTo(request.effectiveTo());
        record.setRecordedAt(recordedAt);
        record.setRawText(request.rawText());
        record.setContentHash(hashService.recordContentHash(
                request.sourceId(),
                request.sourceRevision(),
                request.scoreTenths(),
                request.scoreScale(),
                request.sourceDocNo(),
                request.effectiveFrom(),
                request.effectiveTo(),
                recordedAt,
                request.rawText()));
        record.setOrigin(Origin.MANUAL);
        record.setCreatedAt(Instant.now());
        record = sourceRecordRepository.save(record);

        Instant asOf = Instant.now();
        List<FactEvidence> evidences = sourceRecordRepository
                .findByDossierIdAndSourceIdOrderBySourceRevisionAsc(dossierId, request.sourceId()).stream()
                .map(DossierService::toEvidence)
                .toList();
        Optional<FactEvidence> current = RevisionPolicy.currentValue(evidences, asOf);
        Map<String, String> names = loadNames(List.of(request.sourceId()));
        return toRecordView(record, names.get(request.sourceId()),
                RevisionPolicy.isSuperseded(toEvidence(record), current), asOf);
    }

    @Transactional(readOnly = true)
    public RevisionChainView getRevisions(UUID dossierId, String sourceId) {
        getDossierOrThrow(dossierId);
        dossierSourceRepository.findByDossierIdAndSourceId(dossierId, sourceId)
                .orElseThrow(() -> new ApiException(org.springframework.http.HttpStatus.NOT_FOUND,
                        "SOURCE_NOT_FOUND", "source not bound to dossier: " + sourceId));
        List<SourceRecordEntity> records = sourceRecordRepository
                .findByDossierIdAndSourceIdOrderBySourceRevisionAsc(dossierId, sourceId);
        Map<String, String> names = loadNames(List.of(sourceId));
        Instant asOf = Instant.now();

        List<FactEvidence> evidences = records.stream().map(DossierService::toEvidence).toList();
        Optional<FactEvidence> current = RevisionPolicy.currentValue(evidences, asOf);

        List<RevisionView> revisions = new ArrayList<>();
        Long previousTenths = null;
        for (SourceRecordEntity record : records) {
            SourceRecordView view = toRecordView(record, names.get(sourceId),
                    RevisionPolicy.isSuperseded(toEvidence(record), current), asOf);
            Long deltaTenths = previousTenths == null ? null : record.getScoreTenths() - previousTenths;
            revisions.add(RevisionView.from(view, deltaTenths));
            previousTenths = record.getScoreTenths();
        }
        return new RevisionChainView(sourceId, revisions);
    }

    @Transactional(readOnly = true)
    public List<SealSummaryView> listSeals(UUID dossierId) {
        getDossierOrThrow(dossierId);
        return dossierSealRepository.findByDossierIdOrderBySealedAtAsc(dossierId).stream()
                .map(this::toSealSummary)
                .toList();
    }

    @Transactional
    public SealView createSeal(UUID dossierId, Instant asOf) {
        DossierEntity dossier = getDossierOrThrow(dossierId);
        if (dossierSealRepository.existsByDossierIdAndAsOf(dossierId, asOf)) {
            throw ApiException.sealConflict();
        }
        DossierView view = buildView(dossier, asOf, false);
        SnapshotView snapshot = new SnapshotView(view.status(), view.sources(), view.evidenceChain());

        byte[] canonicalJson;
        try {
            canonicalJson = sealMapper.writeValueAsBytes(snapshot);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize seal snapshot", e);
        }
        Map<String, Object> snapshotMap = sealMapper.convertValue(snapshot,
                new TypeReference<Map<String, Object>>() {
                });

        DossierSealEntity seal = new DossierSealEntity();
        seal.setSealId(UUID.randomUUID());
        seal.setDossierId(dossierId);
        seal.setAsOf(asOf);
        seal.setStatus(view.status().name());
        seal.setSnapshot(snapshotMap);
        seal.setContentHash(hashService.sealContentHash(canonicalJson));
        seal.setSealedAt(Instant.now());
        seal = dossierSealRepository.save(seal);
        return toSealView(dossier, seal);
    }

    @Transactional(readOnly = true)
    public SealView getSeal(UUID dossierId, UUID sealId) {
        DossierEntity dossier = getDossierOrThrow(dossierId);
        DossierSealEntity seal = dossierSealRepository.findBySealIdAndDossierId(sealId, dossierId)
                .orElseThrow(() -> ApiException.sealNotFound(sealId));
        return toSealView(dossier, seal);
    }

    private DossierEntity getDossierOrThrow(UUID dossierId) {
        return dossierRepository.findById(dossierId)
                .orElseThrow(() -> ApiException.dossierNotFound(dossierId));
    }

    private void markBinding(DossierSourceEntity binding, LoadState state, String errorCode, String errorMessage) {
        binding.setLoadState(state);
        binding.setErrorCode(errorCode);
        binding.setErrorMessage(errorMessage);
        dossierSourceRepository.save(binding);
    }

    private SourceRecordEntity newFetchedRecord(UUID dossierId, SourcePublicationPayload payload) {
        SourceRecordEntity record = new SourceRecordEntity();
        record.setDossierId(dossierId);
        record.setSourceId(payload.sourceId());
        record.setSourceRevision(payload.sourceRevision());
        record.setScoreTenths(payload.scoreTenths());
        record.setScoreScale(payload.scoreScale());
        record.setSourceDocNo(payload.sourceDocNo());
        record.setEffectiveFrom(payload.effectiveFrom());
        record.setEffectiveTo(payload.effectiveTo());
        record.setRecordedAt(payload.recordedAt());
        record.setRawText(payload.rawText());
        record.setContentHash(hashService.recordContentHash(
                payload.sourceId(),
                payload.sourceRevision(),
                payload.scoreTenths(),
                payload.scoreScale(),
                payload.sourceDocNo(),
                payload.effectiveFrom(),
                payload.effectiveTo(),
                payload.recordedAt(),
                payload.rawText()));
        record.setOrigin(Origin.FETCH);
        record.setCreatedAt(Instant.now());
        return record;
    }

    private DossierView buildView(DossierEntity dossier, Instant asOf, boolean operationalState) {
        UUID dossierId = dossier.getDossierId();
        List<DossierSourceEntity> bindings = dossierSourceRepository
                .findByDossierIdOrderByPositionAsc(dossierId);
        List<SourceRecordEntity> records = sourceRecordRepository
                .findByDossierIdOrderByRecordedAtAsc(dossierId);
        Map<String, List<SourceRecordEntity>> recordsBySource = records.stream()
                .collect(Collectors.groupingBy(SourceRecordEntity::getSourceId));
        Map<String, String> names = loadNames(bindings.stream()
                .map(DossierSourceEntity::getSourceId)
                .toList());

        Map<String, Optional<FactEvidence>> currentBySource = new HashMap<>();
        List<SourceStateView> sourceStates = new ArrayList<>();
        for (DossierSourceEntity binding : bindings) {
            String sourceId = binding.getSourceId();
            List<SourceRecordEntity> sourceRecords = recordsBySource.getOrDefault(sourceId, List.of());
            List<FactEvidence> evidences = sourceRecords.stream()
                    .map(DossierService::toEvidence)
                    .toList();
            Optional<FactEvidence> current = RevisionPolicy.currentValue(evidences, asOf);
            currentBySource.put(sourceId, current);

            SourceRecordView currentView = current.map(c -> {
                SourceRecordEntity entity = sourceRecords.stream()
                        .filter(r -> r.getSourceRevision() == c.sourceRevision())
                        .findFirst()
                        .orElseThrow();
                return toRecordView(entity, names.get(sourceId), false, asOf);
            }).orElse(null);

            LoadState loadState;
            String errorCode = null;
            String errorMessage = null;
            if (operationalState) {
                loadState = binding.getLoadState();
                errorCode = binding.getErrorCode();
                errorMessage = binding.getErrorMessage();
            } else {
                loadState = current.isPresent() ? LoadState.OK : LoadState.MISSING;
            }

            sourceStates.add(new SourceStateView(
                    sourceId,
                    names.get(sourceId),
                    loadState,
                    errorCode,
                    errorMessage,
                    currentView,
                    sourceRecords.size()));
        }

        List<SourceRecordView> evidenceChain = records.stream()
                .map(entity -> toRecordView(entity,
                        names.get(entity.getSourceId()),
                        RevisionPolicy.isSuperseded(toEvidence(entity),
                                currentBySource.getOrDefault(entity.getSourceId(), Optional.empty())),
                        asOf))
                .toList();

        List<FactEvidence> currentValues = bindings.stream()
                .map(b -> currentBySource.get(b.getSourceId()))
                .flatMap(Optional::stream)
                .toList();
        DossierStatus status = ConsistencyEvaluator.evaluate(currentValues, dossier.getExpectedScale());
        return new DossierView(status, sourceStates, evidenceChain);
    }

    private Map<String, String> loadNames(List<String> sourceIds) {
        if (sourceIds.isEmpty()) {
            return Map.of();
        }
        return sourceRegistryRepository.findAllBySourceIdIn(sourceIds).stream()
                .collect(Collectors.toMap(SourceRegistryEntity::getSourceId,
                        SourceRegistryEntity::getDisplayName));
    }

    private SealSummaryView toSealSummary(DossierSealEntity seal) {
        boolean hasNewerEvidence = sourceRecordRepository
                .existsByDossierIdAndRecordedAtAfter(seal.getDossierId(), seal.getAsOf());
        return new SealSummaryView(
                seal.getSealId(),
                seal.getAsOf(),
                DossierStatus.valueOf(seal.getStatus()),
                seal.getSealedAt(),
                seal.getContentHash(),
                hasNewerEvidence);
    }

    private SealView toSealView(DossierEntity dossier, DossierSealEntity seal) {
        SealSummaryView summary = toSealSummary(seal);
        return new SealView(
                summary.sealId(),
                summary.asOf(),
                summary.status(),
                summary.sealedAt(),
                summary.contentHash(),
                summary.hasNewerEvidence(),
                dossier.getDossierId(),
                factKeyViewOf(dossier),
                seal.getSnapshot());
    }

    private static FactKey factKeyOf(DossierEntity dossier) {
        return new FactKey(
                dossier.getProvinceCode(),
                dossier.getAdmissionYear(),
                dossier.getSubjectCategory(),
                dossier.getSchoolCode(),
                dossier.getMajorGroupCode());
    }

    private static FactKeyView factKeyViewOf(DossierEntity dossier) {
        return new FactKeyView(
                dossier.getProvinceCode(),
                dossier.getAdmissionYear(),
                dossier.getSubjectCategory(),
                dossier.getSchoolCode(),
                dossier.getMajorGroupCode());
    }

    private static FactEvidence toEvidence(SourceRecordEntity entity) {
        return new FactEvidence(
                entity.getSourceId(),
                entity.getSourceRevision(),
                entity.getScoreTenths(),
                entity.getScoreScale(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getRecordedAt());
    }

    private static SourceRecordView toRecordView(SourceRecordEntity entity, String sourceName,
                                                 boolean superseded, Instant asOf) {
        return new SourceRecordView(
                entity.getSourceId(),
                sourceName,
                entity.getSourceRevision(),
                entity.getScoreTenths(),
                Scores.display(entity.getScoreTenths()),
                entity.getScoreScale(),
                entity.getSourceDocNo(),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getRecordedAt(),
                FreshnessPolicy.freshness(entity.getEffectiveFrom(), entity.getEffectiveTo(), asOf),
                entity.getContentHash(),
                entity.getRawText(),
                entity.getOrigin(),
                superseded);
    }

    private record DossierView(DossierStatus status,
                               List<SourceStateView> sources,
                               List<SourceRecordView> evidenceChain) {
    }
}

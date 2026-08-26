package com.dossier.admission.service;

import com.dossier.admission.domain.Dossier;
import com.dossier.admission.domain.DossierEntry;
import com.dossier.admission.domain.DossierStatus;
import com.dossier.admission.domain.FactKey;
import com.dossier.admission.domain.Freshness;
import com.dossier.admission.domain.SourceRecord;
import com.dossier.admission.repository.DossierEntryRepository;
import com.dossier.admission.repository.DossierRepository;
import com.dossier.admission.web.dto.DossierEntryResponse;
import com.dossier.admission.web.dto.DossierResponse;
import com.dossier.admission.web.dto.SourceRecordResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DossierService {

    private final DossierRepository dossierRepository;
    private final DossierEntryRepository dossierEntryRepository;
    private final SourceService sourceService;
    private final ConsistencyEvaluator consistencyEvaluator;

    public DossierService(DossierRepository dossierRepository,
                          DossierEntryRepository dossierEntryRepository,
                          SourceService sourceService,
                          ConsistencyEvaluator consistencyEvaluator) {
        this.dossierRepository = dossierRepository;
        this.dossierEntryRepository = dossierEntryRepository;
        this.sourceService = sourceService;
        this.consistencyEvaluator = consistencyEvaluator;
    }

    @Transactional
    public Dossier createDossier(FactKey factKey, String expectedScale) {
        Dossier dossier = new Dossier(factKey, expectedScale);
        return dossierRepository.save(dossier);
    }

    @Transactional
    public Dossier sealDossier(String dossierId, Instant asOf) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new EntityNotFoundException("卷宗不存在: " + dossierId));

        if (dossier.isSealed()) {
            throw new IllegalStateException("卷宗已封存，不可再次封存: " + dossierId);
        }

        FactKey factKey = dossier.factKey();
        List<SourceRecord> activeAtAsOf = sourceService.getActiveSourcesAsOf(factKey, asOf);

        DossierStatus status = consistencyEvaluator.evaluate(activeAtAsOf, dossier.getExpectedScale());

        for (SourceRecord record : activeAtAsOf) {
            DossierEntry entry = new DossierEntry(dossierId, record);
            dossierEntryRepository.save(entry);
        }

        dossier.updateStatus(status);
        dossier.seal(asOf);
        return dossierRepository.save(dossier);
    }

    @Transactional
    public DossierResponse getDossier(String dossierId) {
        Dossier dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new EntityNotFoundException("卷宗不存在: " + dossierId));

        Instant now = Instant.now();
        FactKey factKey = dossier.factKey();

        if (dossier.isSealed()) {
            List<DossierEntry> entries = dossierEntryRepository.findByDossierIdOrderBySourceId(dossierId);
            List<DossierEntryResponse> entryResponses = entries.stream()
                    .map(e -> DossierEntryResponse.from(e, consistencyEvaluator.freshness(
                            toVirtualRecord(e), dossier.getAsOf() != null ? dossier.getAsOf() : now)))
                    .toList();

            List<SourceRecord> currentActive = sourceService.getActiveSources(factKey);
            boolean expired = isExpired(entries, currentActive);

            return DossierResponse.from(dossier, List.of(), entryResponses, expired);
        } else {
            List<SourceRecord> active = sourceService.getActiveSources(factKey);
            DossierStatus liveStatus = consistencyEvaluator.evaluate(active, dossier.getExpectedScale());
            if (liveStatus != dossier.getStatus()) {
                dossier.updateStatus(liveStatus);
            }
            List<SourceRecordResponse> sourceResponses = active.stream()
                    .map(s -> SourceRecordResponse.from(s, consistencyEvaluator.freshness(s, now), true))
                    .toList();
            return DossierResponse.from(dossier, sourceResponses, List.of(), false);
        }
    }

    @Transactional(readOnly = true)
    public List<Dossier> listDossiers(FactKey factKey) {
        return dossierRepository.findAllByFactKey(factKey);
    }

    private boolean isExpired(List<DossierEntry> sealedEntries, List<SourceRecord> currentActive) {
        if (sealedEntries.size() != currentActive.size()) {
            return true;
        }
        for (int i = 0; i < sealedEntries.size(); i++) {
            DossierEntry sealed = sealedEntries.get(i);
            SourceRecord current = currentActive.get(i);
            if (!sealed.getSourceId().equals(current.getSourceId())
                    || !sealed.getSourceRevision().equals(current.getSourceRevision())
                    || !sealed.getScoreValue().equals(current.getScoreValue())) {
                return true;
            }
        }
        return false;
    }

    private SourceRecord toVirtualRecord(DossierEntry entry) {
        return new SourceRecord(
                new FactKey("", 0, "", "", ""),
                entry.getSourceId(),
                entry.getSourceRevision(),
                entry.getScoreValue(),
                entry.getScoreScale(),
                entry.getOriginalScoreText(),
                entry.getEffectiveFrom(),
                entry.getEffectiveTo(),
                entry.getRecordedAt(),
                entry.getSourceDocumentNo(),
                entry.getOriginalText(),
                entry.getContentHash()
        );
    }
}

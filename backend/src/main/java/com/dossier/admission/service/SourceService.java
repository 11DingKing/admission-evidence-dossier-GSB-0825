package com.dossier.admission.service;

import com.dossier.admission.domain.FactKey;
import com.dossier.admission.domain.SourceRecord;
import com.dossier.admission.repository.SourceRecordRepository;
import com.dossier.admission.web.dto.BatchSourceResponse;
import com.dossier.admission.web.dto.SourceError;
import com.dossier.admission.web.dto.SourceRecordRequest;
import com.dossier.admission.web.dto.SourceRecordResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SourceService {

    private final SourceRecordRepository sourceRecordRepository;
    private final ConsistencyEvaluator consistencyEvaluator;

    public SourceService(SourceRecordRepository sourceRecordRepository,
                         ConsistencyEvaluator consistencyEvaluator) {
        this.sourceRecordRepository = sourceRecordRepository;
        this.consistencyEvaluator = consistencyEvaluator;
    }

    @Transactional
    public BatchSourceResponse submitBatch(List<SourceRecordRequest> requests, FactKey expectedFactKey) {
        List<SourceRecordResponse> accepted = new ArrayList<>();
        List<SourceError> errors = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            SourceRecordRequest req = requests.get(i);
            FactKey factKey = new FactKey(
                    req.provinceCode(), req.admissionYear(), req.subjectCategory(),
                    req.schoolCode(), req.majorGroupCode()
            );

            if (expectedFactKey != null && !expectedFactKey.equals(factKey)) {
                errors.add(new SourceError(i, req.sourceId(), "FACT_KEY_MISMATCH",
                        "来源的 fact_key 与卷宗不匹配: " + factKey + " != " + expectedFactKey));
                continue;
            }

            if (sourceRecordRepository.countByFactKeyAndSourceIdAndRevision(
                    factKey, req.sourceId(), req.sourceRevision()) > 0) {
                errors.add(new SourceError(i, req.sourceId(), "DUPLICATE_REVISION",
                        "来源 " + req.sourceId() + " revision " + req.sourceRevision() + " 已存在"));
                continue;
            }

            String content = buildContentHash(req);
            SourceRecord record = new SourceRecord(
                    factKey,
                    req.sourceId(),
                    req.sourceRevision(),
                    req.scoreValue(),
                    req.scoreScale(),
                    req.originalScoreText(),
                    req.effectiveFrom(),
                    req.effectiveTo(),
                    req.recordedAt(),
                    req.sourceDocumentNo(),
                    req.originalText(),
                    content
            );
            SourceRecord saved = sourceRecordRepository.save(record);

            Integer maxRevision = sourceRecordRepository.findMaxRevision(factKey, req.sourceId());
            boolean isLatest = maxRevision != null && maxRevision.equals(saved.getSourceRevision());

            accepted.add(SourceRecordResponse.from(
                    saved,
                    consistencyEvaluator.freshness(saved, saved.getRecordedAt()),
                    isLatest
            ));
        }

        int total = requests.size();
        int success = accepted.size();
        int failure = errors.size();
        return new BatchSourceResponse(accepted, errors, total, success, failure, failure > 0 && success > 0);
    }

    @Transactional(readOnly = true)
    public List<SourceRecord> getActiveSources(FactKey factKey) {
        return sourceRecordRepository.findActiveByFactKey(factKey);
    }

    @Transactional(readOnly = true)
    public List<SourceRecord> getActiveSourcesAsOf(FactKey factKey, java.time.Instant asOf) {
        return sourceRecordRepository.findActiveByFactKeyAsOf(factKey, asOf);
    }

    @Transactional(readOnly = true)
    public List<SourceRecord> getAllSources(FactKey factKey) {
        return sourceRecordRepository.findAllByFactKey(factKey);
    }

    private String buildContentHash(SourceRecordRequest req) {
        String raw = (req.originalText() != null ? req.originalText() : "")
                + "|" + req.sourceId()
                + "|" + req.sourceRevision()
                + "|" + req.scoreValue()
                + "|" + req.scoreScale()
                + "|" + req.sourceDocumentNo();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

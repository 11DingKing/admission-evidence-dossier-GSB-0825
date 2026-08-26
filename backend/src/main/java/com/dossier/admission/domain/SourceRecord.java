package com.dossier.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "source_record")
public class SourceRecord {

    @EmbeddedId
    private SourceRecordId id;

    @Column(name = "score_value", nullable = false)
    private Long scoreValue;

    @Column(name = "score_scale", nullable = false, length = 64)
    private String scoreScale;

    @Column(name = "original_score_text", length = 64)
    private String originalScoreText;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "source_document_no", length = 128)
    private String sourceDocumentNo;

    @Column(name = "original_text", columnDefinition = "text")
    private String originalText;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected SourceRecord() {
    }

    public SourceRecord(FactKey factKey, String sourceId, Integer sourceRevision,
                        Long scoreValue, String scoreScale, String originalScoreText,
                        Instant effectiveFrom, Instant effectiveTo, Instant recordedAt,
                        String sourceDocumentNo, String originalText, String contentHash) {
        this.id = new SourceRecordId(factKey, sourceId, sourceRevision);
        this.scoreValue = scoreValue;
        this.scoreScale = scoreScale;
        this.originalScoreText = originalScoreText;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.recordedAt = recordedAt;
        this.sourceDocumentNo = sourceDocumentNo;
        this.originalText = originalText;
        this.contentHash = contentHash;
        this.createdAt = Instant.now();
    }

    public SourceRecordId getId() { return id; }
    public FactKey getFactKey() { return id.getFactKey(); }
    public String getSourceId() { return id.getSourceId(); }
    public Integer getSourceRevision() { return id.getSourceRevision(); }
    public Long getScoreValue() { return scoreValue; }
    public String getScoreScale() { return scoreScale; }
    public String getOriginalScoreText() { return originalScoreText; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getSourceDocumentNo() { return sourceDocumentNo; }
    public String getOriginalText() { return originalText; }
    public String getContentHash() { return contentHash; }
    public Instant getCreatedAt() { return createdAt; }
}

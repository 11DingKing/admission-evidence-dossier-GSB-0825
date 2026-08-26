package com.dossier.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "dossier_entry")
public class DossierEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_id", nullable = false, length = 36)
    private String dossierId;

    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;

    @Column(name = "source_revision", nullable = false)
    private Integer sourceRevision;

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

    protected DossierEntry() {
    }

    public DossierEntry(String dossierId, SourceRecord record) {
        this.dossierId = dossierId;
        this.sourceId = record.getSourceId();
        this.sourceRevision = record.getSourceRevision();
        this.scoreValue = record.getScoreValue();
        this.scoreScale = record.getScoreScale();
        this.originalScoreText = record.getOriginalScoreText();
        this.effectiveFrom = record.getEffectiveFrom();
        this.effectiveTo = record.getEffectiveTo();
        this.recordedAt = record.getRecordedAt();
        this.sourceDocumentNo = record.getSourceDocumentNo();
        this.originalText = record.getOriginalText();
        this.contentHash = record.getContentHash();
    }

    public Long getId() { return id; }
    public String getDossierId() { return dossierId; }
    public String getSourceId() { return sourceId; }
    public Integer getSourceRevision() { return sourceRevision; }
    public Long getScoreValue() { return scoreValue; }
    public String getScoreScale() { return scoreScale; }
    public String getOriginalScoreText() { return originalScoreText; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getSourceDocumentNo() { return sourceDocumentNo; }
    public String getOriginalText() { return originalText; }
    public String getContentHash() { return contentHash; }
}

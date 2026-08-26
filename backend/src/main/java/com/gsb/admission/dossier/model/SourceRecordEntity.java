package com.gsb.admission.dossier.model;

import com.gsb.admission.dossier.domain.Origin;
import com.gsb.admission.dossier.domain.ScoreScale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "source_record")
public class SourceRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "dossier_id", nullable = false, updatable = false)
    private UUID dossierId;

    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Column(name = "source_revision", nullable = false, updatable = false)
    private int sourceRevision;

    @Column(name = "score_tenths", nullable = false, updatable = false)
    private long scoreTenths;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_scale", nullable = false, updatable = false)
    private ScoreScale scoreScale;

    @Column(name = "source_doc_no", nullable = false, updatable = false)
    private String sourceDocNo;

    @Column(name = "effective_from", nullable = false, updatable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to", updatable = false)
    private LocalDate effectiveTo;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @Column(name = "raw_text", nullable = false, updatable = false)
    private String rawText;

    @Column(name = "content_hash", nullable = false, updatable = false, length = 73)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, updatable = false)
    private Origin origin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDossierId() {
        return dossierId;
    }

    public void setDossierId(UUID dossierId) {
        this.dossierId = dossierId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getSourceRevision() {
        return sourceRevision;
    }

    public void setSourceRevision(int sourceRevision) {
        this.sourceRevision = sourceRevision;
    }

    public long getScoreTenths() {
        return scoreTenths;
    }

    public void setScoreTenths(long scoreTenths) {
        this.scoreTenths = scoreTenths;
    }

    public ScoreScale getScoreScale() {
        return scoreScale;
    }

    public void setScoreScale(ScoreScale scoreScale) {
        this.scoreScale = scoreScale;
    }

    public String getSourceDocNo() {
        return sourceDocNo;
    }

    public void setSourceDocNo(String sourceDocNo) {
        this.sourceDocNo = sourceDocNo;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

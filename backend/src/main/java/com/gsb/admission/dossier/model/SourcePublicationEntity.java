package com.gsb.admission.dossier.model;

import com.gsb.admission.dossier.domain.ScoreScale;
import com.gsb.admission.dossier.domain.SubjectCategory;
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
@Table(name = "source_publication")
public class SourcePublicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Column(name = "province_code", nullable = false, updatable = false)
    private String provinceCode;

    @Column(name = "admission_year", nullable = false, updatable = false)
    private int admissionYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_category", nullable = false, updatable = false)
    private SubjectCategory subjectCategory;

    @Column(name = "school_code", nullable = false, updatable = false)
    private String schoolCode;

    @Column(name = "major_group_code", nullable = false, updatable = false)
    private String majorGroupCode;

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

    @Column(name = "available", nullable = false)
    private boolean available = true;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public int getAdmissionYear() {
        return admissionYear;
    }

    public void setAdmissionYear(int admissionYear) {
        this.admissionYear = admissionYear;
    }

    public SubjectCategory getSubjectCategory() {
        return subjectCategory;
    }

    public void setSubjectCategory(SubjectCategory subjectCategory) {
        this.subjectCategory = subjectCategory;
    }

    public String getSchoolCode() {
        return schoolCode;
    }

    public void setSchoolCode(String schoolCode) {
        this.schoolCode = schoolCode;
    }

    public String getMajorGroupCode() {
        return majorGroupCode;
    }

    public void setMajorGroupCode(String majorGroupCode) {
        this.majorGroupCode = majorGroupCode;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

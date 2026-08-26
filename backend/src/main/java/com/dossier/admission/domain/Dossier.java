package com.dossier.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dossier")
public class Dossier {

    @Id
    @Column(name = "dossier_id", nullable = false, length = 36)
    private String dossierId;

    @Column(name = "province_code", nullable = false, length = 12)
    private String provinceCode;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    @Column(name = "subject_category", nullable = false, length = 32)
    private String subjectCategory;

    @Column(name = "school_code", nullable = false, length = 16)
    private String schoolCode;

    @Column(name = "major_group_code", nullable = false, length = 32)
    private String majorGroupCode;

    @Column(name = "expected_scale", nullable = false, length = 64)
    private String expectedScale;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DossierStatus status;

    @Column(name = "as_of")
    private Instant asOf;

    @Column(name = "sealed", nullable = false)
    private boolean sealed;

    @Column(name = "sealed_at")
    private Instant sealedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Dossier() {
    }

    public Dossier(FactKey factKey, String expectedScale) {
        this.dossierId = UUID.randomUUID().toString();
        this.provinceCode = factKey.getProvinceCode();
        this.admissionYear = factKey.getAdmissionYear();
        this.subjectCategory = factKey.getSubjectCategory();
        this.schoolCode = factKey.getSchoolCode();
        this.majorGroupCode = factKey.getMajorGroupCode();
        this.expectedScale = expectedScale;
        this.status = DossierStatus.PENDING;
        this.sealed = false;
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public FactKey factKey() {
        return new FactKey(provinceCode, admissionYear, subjectCategory, schoolCode, majorGroupCode);
    }

    public void seal(Instant asOf) {
        this.asOf = asOf;
        this.sealed = true;
        this.sealedAt = Instant.now();
        this.updatedAt = this.sealedAt;
    }

    public void updateStatus(DossierStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public String getDossierId() { return dossierId; }
    public String getProvinceCode() { return provinceCode; }
    public Integer getAdmissionYear() { return admissionYear; }
    public String getSubjectCategory() { return subjectCategory; }
    public String getSchoolCode() { return schoolCode; }
    public String getMajorGroupCode() { return majorGroupCode; }
    public String getExpectedScale() { return expectedScale; }
    public DossierStatus getStatus() { return status; }
    public Instant getAsOf() { return asOf; }
    public boolean isSealed() { return sealed; }
    public Instant getSealedAt() { return sealedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

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
import java.util.UUID;

@Entity
@Table(name = "dossier")
public class DossierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dossier_id", nullable = false, updatable = false)
    private UUID dossierId;

    @Column(name = "province_code", nullable = false)
    private String provinceCode;

    @Column(name = "admission_year", nullable = false)
    private int admissionYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_category", nullable = false)
    private SubjectCategory subjectCategory;

    @Column(name = "school_code", nullable = false)
    private String schoolCode;

    @Column(name = "major_group_code", nullable = false)
    private String majorGroupCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "expected_scale", nullable = false)
    private ScoreScale expectedScale;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getDossierId() {
        return dossierId;
    }

    public void setDossierId(UUID dossierId) {
        this.dossierId = dossierId;
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

    public ScoreScale getExpectedScale() {
        return expectedScale;
    }

    public void setExpectedScale(ScoreScale expectedScale) {
        this.expectedScale = expectedScale;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

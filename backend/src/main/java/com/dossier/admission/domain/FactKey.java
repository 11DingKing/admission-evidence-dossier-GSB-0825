package com.dossier.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FactKey implements Serializable {

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

    protected FactKey() {
    }

    public FactKey(String provinceCode, Integer admissionYear, String subjectCategory,
                   String schoolCode, String majorGroupCode) {
        this.provinceCode = provinceCode;
        this.admissionYear = admissionYear;
        this.subjectCategory = subjectCategory;
        this.schoolCode = schoolCode;
        this.majorGroupCode = majorGroupCode;
    }

    public String getProvinceCode() { return provinceCode; }
    public Integer getAdmissionYear() { return admissionYear; }
    public String getSubjectCategory() { return subjectCategory; }
    public String getSchoolCode() { return schoolCode; }
    public String getMajorGroupCode() { return majorGroupCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FactKey f)) return false;
        return Objects.equals(provinceCode, f.provinceCode)
                && Objects.equals(admissionYear, f.admissionYear)
                && Objects.equals(subjectCategory, f.subjectCategory)
                && Objects.equals(schoolCode, f.schoolCode)
                && Objects.equals(majorGroupCode, f.majorGroupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provinceCode, admissionYear, subjectCategory, schoolCode, majorGroupCode);
    }

    @Override
    public String toString() {
        return provinceCode + ":" + admissionYear + ":" + subjectCategory + ":"
                + schoolCode + ":" + majorGroupCode;
    }
}

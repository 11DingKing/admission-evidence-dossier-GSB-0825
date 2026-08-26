package com.gsb.admission.dossier.domain;

public record FactKey(
        String provinceCode,
        int admissionYear,
        SubjectCategory subjectCategory,
        String schoolCode,
        String majorGroupCode) {

    public String canonical() {
        return provinceCode + "|"
                + admissionYear + "|"
                + subjectCategory.name() + "|"
                + schoolCode + "|"
                + majorGroupCode;
    }

    public ScoreScale expectedScale() {
        return switch (subjectCategory) {
            case PHYSICS, HISTORY -> ScoreScale.GAOKAO_750_TENTH;
            case ART_COMPOSITE -> ScoreScale.ART_COMPOSITE_TENTH;
        };
    }

    public boolean sameFactAs(FactKey other) {
        return this.provinceCode.equals(other.provinceCode)
                && this.admissionYear == other.admissionYear
                && this.subjectCategory == other.subjectCategory
                && this.schoolCode.equals(other.schoolCode)
                && this.majorGroupCode.equals(other.majorGroupCode);
    }
}

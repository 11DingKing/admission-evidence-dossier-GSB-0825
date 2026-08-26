package com.dossier.admission.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SourceRecordId implements Serializable {

    private FactKey factKey;

    @Column(name = "source_id", nullable = false, length = 64)
    private String sourceId;

    @Column(name = "source_revision", nullable = false)
    private Integer sourceRevision;

    protected SourceRecordId() {
    }

    public SourceRecordId(FactKey factKey, String sourceId, Integer sourceRevision) {
        this.factKey = factKey;
        this.sourceId = sourceId;
        this.sourceRevision = sourceRevision;
    }

    public FactKey getFactKey() { return factKey; }
    public String getSourceId() { return sourceId; }
    public Integer getSourceRevision() { return sourceRevision; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SourceRecordId that)) return false;
        return Objects.equals(factKey, that.factKey)
                && Objects.equals(sourceId, that.sourceId)
                && Objects.equals(sourceRevision, that.sourceRevision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factKey, sourceId, sourceRevision);
    }
}

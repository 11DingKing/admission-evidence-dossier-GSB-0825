package com.gsb.admission.dossier.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class DossierSourceId implements Serializable {

    private UUID dossierId;
    private String sourceId;

    public DossierSourceId() {
    }

    public DossierSourceId(UUID dossierId, String sourceId) {
        this.dossierId = dossierId;
        this.sourceId = sourceId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DossierSourceId that)) {
            return false;
        }
        return Objects.equals(dossierId, that.dossierId) && Objects.equals(sourceId, that.sourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dossierId, sourceId);
    }
}

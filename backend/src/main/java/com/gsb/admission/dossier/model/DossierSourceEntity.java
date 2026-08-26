package com.gsb.admission.dossier.model;

import com.gsb.admission.dossier.domain.LoadState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "dossier_source")
@IdClass(DossierSourceId.class)
public class DossierSourceEntity {

    @Id
    @Column(name = "dossier_id", nullable = false, updatable = false)
    private java.util.UUID dossierId;

    @Id
    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Column(name = "position", nullable = false)
    private int position;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_state", nullable = false)
    private LoadState loadState = LoadState.MISSING;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    public java.util.UUID getDossierId() {
        return dossierId;
    }

    public void setDossierId(java.util.UUID dossierId) {
        this.dossierId = dossierId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public LoadState getLoadState() {
        return loadState;
    }

    public void setLoadState(LoadState loadState) {
        this.loadState = loadState;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}

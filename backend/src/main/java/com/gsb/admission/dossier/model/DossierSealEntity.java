package com.gsb.admission.dossier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "dossier_seal")
public class DossierSealEntity {

    @Id
    @Column(name = "seal_id", nullable = false, updatable = false)
    private UUID sealId;

    @Column(name = "dossier_id", nullable = false, updatable = false)
    private UUID dossierId;

    @Column(name = "as_of", nullable = false, updatable = false)
    private Instant asOf;

    @Column(name = "status", nullable = false, updatable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot", nullable = false, updatable = false, columnDefinition = "jsonb")
    private Map<String, Object> snapshot;

    @Column(name = "content_hash", nullable = false, updatable = false, length = 73)
    private String contentHash;

    @Column(name = "sealed_at", nullable = false, updatable = false)
    private Instant sealedAt;

    public UUID getSealId() {
        return sealId;
    }

    public void setSealId(UUID sealId) {
        this.sealId = sealId;
    }

    public UUID getDossierId() {
        return dossierId;
    }

    public void setDossierId(UUID dossierId) {
        this.dossierId = dossierId;
    }

    public Instant getAsOf() {
        return asOf;
    }

    public void setAsOf(Instant asOf) {
        this.asOf = asOf;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, Object> snapshot) {
        this.snapshot = snapshot;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Instant getSealedAt() {
        return sealedAt;
    }

    public void setSealedAt(Instant sealedAt) {
        this.sealedAt = sealedAt;
    }
}

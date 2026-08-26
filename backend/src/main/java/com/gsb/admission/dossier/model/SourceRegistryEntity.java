package com.gsb.admission.dossier.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "source_registry")
public class SourceRegistryEntity {

    @Id
    @Column(name = "source_id", nullable = false, updatable = false)
    private String sourceId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

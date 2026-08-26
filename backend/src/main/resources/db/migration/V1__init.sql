CREATE TABLE dossier (
    dossier_id        VARCHAR(36)  PRIMARY KEY,
    province_code     VARCHAR(12)  NOT NULL,
    admission_year    INTEGER      NOT NULL,
    subject_category  VARCHAR(32)  NOT NULL,
    school_code       VARCHAR(16)  NOT NULL,
    major_group_code  VARCHAR(32)  NOT NULL,
    expected_scale    VARCHAR(64)  NOT NULL,
    status            VARCHAR(32)  NOT NULL,
    as_of             TIMESTAMPTZ,
    sealed            BOOLEAN      NOT NULL DEFAULT FALSE,
    sealed_at         TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_dossier_fact_key ON dossier (
    province_code, admission_year, subject_category, school_code, major_group_code
);

CREATE TABLE source_record (
    province_code       VARCHAR(12)  NOT NULL,
    admission_year      INTEGER      NOT NULL,
    subject_category    VARCHAR(32)  NOT NULL,
    school_code         VARCHAR(16)  NOT NULL,
    major_group_code    VARCHAR(32)  NOT NULL,
    source_id           VARCHAR(64)  NOT NULL,
    source_revision     INTEGER      NOT NULL,
    score_value         BIGINT       NOT NULL,
    score_scale         VARCHAR(64)  NOT NULL,
    original_score_text VARCHAR(64),
    effective_from      TIMESTAMPTZ  NOT NULL,
    effective_to        TIMESTAMPTZ,
    recorded_at         TIMESTAMPTZ  NOT NULL,
    source_document_no  VARCHAR(128),
    original_text       TEXT,
    content_hash        VARCHAR(64)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (
        province_code, admission_year, subject_category,
        school_code, major_group_code, source_id, source_revision
    )
);

CREATE INDEX idx_source_record_fact_key ON source_record (
    province_code, admission_year, subject_category, school_code, major_group_code
);

CREATE INDEX idx_source_record_source ON source_record (
    province_code, admission_year, subject_category,
    school_code, major_group_code, source_id, source_revision DESC
);

CREATE TABLE dossier_entry (
    id                  BIGSERIAL    PRIMARY KEY,
    dossier_id          VARCHAR(36)  NOT NULL REFERENCES dossier(dossier_id),
    source_id           VARCHAR(64)  NOT NULL,
    source_revision     INTEGER      NOT NULL,
    score_value         BIGINT       NOT NULL,
    score_scale         VARCHAR(64)  NOT NULL,
    original_score_text VARCHAR(64),
    effective_from      TIMESTAMPTZ  NOT NULL,
    effective_to        TIMESTAMPTZ,
    recorded_at         TIMESTAMPTZ  NOT NULL,
    source_document_no  VARCHAR(128),
    original_text       TEXT,
    content_hash        VARCHAR(64)  NOT NULL
);

CREATE INDEX idx_dossier_entry_dossier ON dossier_entry (dossier_id);

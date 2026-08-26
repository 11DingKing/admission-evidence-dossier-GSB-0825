create table source_registry (
    source_id    varchar primary key,
    display_name varchar not null,
    provider     varchar not null,
    created_at   timestamptz not null default now()
);

create table dossier (
    dossier_id        uuid primary key default gen_random_uuid(),
    province_code     varchar not null,
    admission_year    int not null,
    subject_category  varchar not null,
    school_code       varchar not null,
    major_group_code  varchar not null,
    expected_scale    varchar not null,
    created_at        timestamptz not null default now(),
    constraint uq_dossier_fact_key unique (province_code, admission_year, subject_category, school_code, major_group_code)
);

create table dossier_source (
    dossier_id    uuid not null references dossier(dossier_id),
    source_id     varchar not null references source_registry(source_id),
    position      int not null,
    load_state    varchar not null default 'MISSING',
    error_code    varchar,
    error_message varchar,
    primary key (dossier_id, source_id)
);

create table source_record (
    id              uuid primary key default gen_random_uuid(),
    dossier_id      uuid not null references dossier(dossier_id),
    source_id       varchar not null,
    source_revision int not null,
    score_tenths    bigint not null,
    score_scale     varchar not null,
    source_doc_no   varchar not null,
    effective_from  date not null,
    effective_to    date,
    recorded_at     timestamptz not null,
    raw_text        text not null,
    content_hash    varchar(73) not null,
    origin          varchar not null,
    created_at      timestamptz not null default now(),
    constraint uq_source_record_revision unique (dossier_id, source_id, source_revision)
);

create table dossier_seal (
    seal_id      uuid primary key,
    dossier_id   uuid not null references dossier(dossier_id),
    as_of        timestamptz not null,
    status       varchar not null,
    snapshot     jsonb not null,
    content_hash varchar(73) not null,
    sealed_at    timestamptz not null default now(),
    constraint uq_dossier_seal_as_of unique (dossier_id, as_of)
);

create table source_publication (
    id                uuid primary key default gen_random_uuid(),
    source_id         varchar not null references source_registry(source_id),
    province_code     varchar not null,
    admission_year    int not null,
    subject_category  varchar not null,
    school_code       varchar not null,
    major_group_code  varchar not null,
    source_revision   int not null,
    score_tenths      bigint not null,
    score_scale       varchar not null,
    source_doc_no     varchar not null,
    effective_from    date not null,
    effective_to      date,
    recorded_at       timestamptz not null,
    raw_text          text not null,
    available         boolean not null default true,
    error_code        varchar,
    created_at        timestamptz not null default now(),
    constraint uq_source_publication_revision unique (source_id, province_code, admission_year, subject_category, school_code, major_group_code, source_revision)
);

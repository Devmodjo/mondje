CREATE TABLE imports (
    id               UUID PRIMARY KEY,
    file_name        VARCHAR(255) NOT NULL,
    file_hash        CHAR(64)     NOT NULL,
    format           VARCHAR(10)  NOT NULL CHECK (format IN ('TXT', 'NDJSON')),
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'INTERRUPTED', 'CANCELLED', 'FAILED')),
    total_lines      BIGINT       NOT NULL DEFAULT 0,
    inserted_count   BIGINT       NOT NULL DEFAULT 0,
    duplicate_count  BIGINT       NOT NULL DEFAULT 0,
    error_count      BIGINT       NOT NULL DEFAULT 0,
    last_line        BIGINT       NOT NULL DEFAULT 0,
    last_byte_offset BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    finished_at      TIMESTAMPTZ,
    CONSTRAINT uq_imports_file_hash UNIQUE (file_hash)
);

CREATE TABLE transactions (
    id                UUID PRIMARY KEY,
    type              VARCHAR(6)   NOT NULL CHECK (type IN ('CREDIT', 'DEBIT')),
    amount            BIGINT       NOT NULL CHECK (amount > 0),
    description       VARCHAR(255) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    import_id         UUID REFERENCES imports (id),
    source_line       BIGINT,
    modified_manually BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at        TIMESTAMPTZ
);

CREATE TABLE import_errors (
    id          BIGSERIAL PRIMARY KEY,
    import_id   UUID   NOT NULL REFERENCES imports (id),
    line_number BIGINT NOT NULL,
    byte_offset BIGINT NOT NULL,
    raw_line    TEXT   NOT NULL,
    reason      VARCHAR(255) NOT NULL
);

CREATE INDEX idx_tx_created_id ON transactions (created_at DESC, id DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_tx_balance ON transactions (created_at) INCLUDE (type, amount) WHERE deleted_at IS NULL;
CREATE INDEX idx_tx_import ON transactions (import_id);
CREATE INDEX idx_tx_deleted ON transactions (deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_errors_import ON import_errors (import_id, line_number);
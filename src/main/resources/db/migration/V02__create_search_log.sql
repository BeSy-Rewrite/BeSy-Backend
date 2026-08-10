CREATE TABLE IF NOT EXISTS migrated_data.search_log (
    id           bigserial PRIMARY KEY,
    query        text        NOT NULL,
    filters      text        NULL,
    result_count bigint      NOT NULL,
    user_id      bigint      NULL,
    searched_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_search_log_searched_at
    ON migrated_data.search_log (searched_at);

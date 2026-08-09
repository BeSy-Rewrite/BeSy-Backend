CREATE TABLE IF NOT EXISTS search_log (
    id           bigserial PRIMARY KEY,
    query        text        NOT NULL,
    filters      text        NULL,
    result_count bigint      NOT NULL,
    user_id      bigint      NULL,
    searched_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_search_log_searched_at
    ON search_log (searched_at);

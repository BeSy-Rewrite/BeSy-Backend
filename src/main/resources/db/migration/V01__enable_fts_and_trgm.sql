-- Extensions (idempotent; require appropriate DB privileges)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
ALTER EXTENSION pg_trgm SET SCHEMA public;

-- FTS + trigram columns on the order table
ALTER TABLE migrated_data."order" ADD COLUMN IF NOT EXISTS search_vector tsvector;
ALTER TABLE migrated_data."order" ADD COLUMN IF NOT EXISTS search_text   text;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_order_search_vector
    ON migrated_data."order" USING gin (search_vector);

CREATE INDEX IF NOT EXISTS idx_order_search_text_trgm
    ON migrated_data."order" USING gin (search_text gin_trgm_ops);

-- Unique index for keycloak_uuid (partial: multiple users can have null keycloak_uuid)
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_keycloak_uuid
    ON migrated_data."user" (keycloak_uuid)
    WHERE keycloak_uuid IS NOT NULL;

-- Unique index for email
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_email
    ON migrated_data."user" (email);

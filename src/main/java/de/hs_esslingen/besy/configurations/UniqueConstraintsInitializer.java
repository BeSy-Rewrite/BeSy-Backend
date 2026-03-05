package de.hs_esslingen.besy.configurations;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes unique constraints after Hibernate schema generation.
 * This ensures that unique constraints are added after tables are created,
 * making the operation idempotent and safe for multi-instance deployments.
 */
@Component
public class UniqueConstraintsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(UniqueConstraintsInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public UniqueConstraintsInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Executes after application is fully started and Hibernate has created/updated
     * the schema. Adds unique constraints for user table fields.
     *
     * This operation is idempotent and safe for multi-instance deployments.
     * The CREATE INDEX IF NOT EXISTS statements are safe under concurrent startup.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void addUniqueConstraints() {
        try {
            logger.info("Attempting to add unique constraints for user table (idempotent)...");

            // Unique index for keycloak_uuid (allows multiple NULL values)
            String sql1 = "CREATE UNIQUE INDEX IF NOT EXISTS uk_user_keycloak_uuid " +
                    "ON migrated_data.\"user\" (keycloak_uuid) " +
                    "WHERE keycloak_uuid IS NOT NULL";

            jdbcTemplate.execute(sql1);
            logger.debug("Successfully ensured unique constraint uk_user_keycloak_uuid exists");

            // Unique index for email
            String sql2 = "CREATE UNIQUE INDEX IF NOT EXISTS uk_user_email " +
                    "ON migrated_data.\"user\" (email)";

            jdbcTemplate.execute(sql2);
            logger.debug("Successfully ensured unique constraint uk_user_email exists");

            logIndexPresence("uk_user_keycloak_uuid");
            logIndexPresence("uk_user_email");

            logger.info("Successfully ensured all unique constraints exist for user table");
        } catch (Exception e) {
            logger.error("Failed to add unique constraints: {}", e.getMessage(), e);
        }
    }

    private void logIndexPresence(String indexName) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT to_regclass('migrated_data.' || ?) IS NOT NULL",
                Boolean.class,
                indexName);

        if (Boolean.TRUE.equals(exists)) {
            logger.info("Validation successful: index {} exists on migrated_data.\"user\"", indexName);
        } else {
            logger.warn("Validation failed: index {} does not exist on migrated_data.\"user\"", indexName);
        }
    }
}

package de.hs_esslingen.besy.configurations;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Initializes database constraints after Hibernate schema generation.
 * This ensures that foreign key constraints are added after tables are created.
 */
@Component
public class DatabaseConstraintsInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConstraintsInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintsInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Executes after application is fully started and Hibernate has created/updated
     * the schema.
     * Adds composite foreign key constraint for order.customer_id if it doesn't
     * exist.
     * 
     * This operation is idempotent and safe for multi-instance deployments.
     * The PL/pgSQL block catches duplicate constraint exceptions from concurrent
     * invocations and handles them gracefully.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void addConstraints() {
        try {
            logger.info("Attempting to add foreign key constraint fk_order_customer_id_supplier_id (idempotent)...");

            String sql = "DO $$ BEGIN\n" +
                    "  ALTER TABLE migrated_data.\"order\" " +
                    "  ADD CONSTRAINT fk_order_customer_id_supplier_id " +
                    "  FOREIGN KEY (customer_id, supplier_id) " +
                    "  REFERENCES migrated_data.customer_id(customer_id, supplier_id) " +
                    "  ON UPDATE CASCADE " +
                    "  ON DELETE SET NULL;\n" +
                    "EXCEPTION WHEN duplicate_object THEN\n" +
                    "  NULL;\n" +
                    "END $$;";

            jdbcTemplate.execute(sql);

            logger.info("Successfully ensured foreign key constraint fk_order_customer_id_supplier_id exists");
        } catch (Exception e) {
            logger.error("Failed to add foreign key constraint: {}", e.getMessage(), e);
        }
    }
}

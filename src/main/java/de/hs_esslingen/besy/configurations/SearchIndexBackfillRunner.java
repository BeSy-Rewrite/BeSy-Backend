package de.hs_esslingen.besy.configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import de.hs_esslingen.besy.services.OrderService;

/**
 * One-time backfill of the FTS columns (search_vector / search_text) for
 * orders that were migrated before the FTS feature existed.
 *
 * Idempotent: only runs when there are orders with a NULL search_vector,
 * so restarts / multi-instance deployments won't redo the work unnecessarily.
 * Can be disabled via 'besy.search.backfill.enabled=false'.
 */
@Component
public class SearchIndexBackfillRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexBackfillRunner.class);

    private final OrderService orderService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${besy.search.backfill.enabled:true}")
    private boolean enabled;

    public SearchIndexBackfillRunner(OrderService orderService, JdbcTemplate jdbcTemplate) {
        this.orderService = orderService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            logger.info("Search index backfill disabled via property, skipping.");
            return;
        }

        try {
            Long pending = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM migrated_data.\"order\" WHERE search_vector IS NULL",
                    Long.class);

            if (pending == null || pending == 0) {
                logger.info("Search index backfill: nothing to do (all orders already indexed).");
                return;
            }

            logger.info("Search index backfill: {} orders without search_vector, running refresh...", pending);
            orderService.refreshAllSearchIndexes();
            logger.info("Search index backfill completed.");
        } catch (Exception e) {
            logger.error("Search index backfill failed: {}", e.getMessage(), e);
        }
    }
}

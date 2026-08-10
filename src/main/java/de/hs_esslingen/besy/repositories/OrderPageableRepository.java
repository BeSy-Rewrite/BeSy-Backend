package de.hs_esslingen.besy.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import de.hs_esslingen.besy.models.Order;

public interface OrderPageableRepository
        extends PagingAndSortingRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    @Query(value = """
            SELECT o.*
            FROM migrated_data."order" o
            WHERE o.search_vector @@ websearch_to_tsquery('german', :q)
               OR similarity(o.search_text, :q) > 0.2
            ORDER BY
                ts_rank(o.search_vector, websearch_to_tsquery('german', :q))
                + similarity(o.search_text, :q) DESC
            """, countQuery = """
            SELECT count(*)
            FROM migrated_data."order" o
            WHERE o.search_vector @@ websearch_to_tsquery('german', :q)
               OR similarity(o.search_text, :q) > 0.2
            """, nativeQuery = true)
    Page<Order> searchByRelevance(@Param("q") String q, Pageable pageable);
}

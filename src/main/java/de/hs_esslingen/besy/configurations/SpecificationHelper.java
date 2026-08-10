package de.hs_esslingen.besy.configurations;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import de.hs_esslingen.besy.models.Order;

public class SpecificationHelper {

    private SpecificationHelper() {
    }

    public static <T> Specification<Order> contains(List<T> attributeList,
            String attributeName) {
        return (root, query, criteriaBuilder) -> {
            if (attributeList == null || attributeList.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get(attributeName).in(attributeList);
        };
    }

    public static <T extends Comparable<? super T>> Specification<Order> isBetween(
            T min, T max, String attributeName) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return criteriaBuilder.conjunction();
            } else if (min != null && max != null) {
                return criteriaBuilder.between(root.get(attributeName), min, max);
            } else if (min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(attributeName), min);
            } else if (max != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get(attributeName), max);
            } else {
                return criteriaBuilder.conjunction();
            }
        };
    }

    public static Specification<Order> fullTextSearch(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction(); // no-op
        }
        String q = searchQuery.trim();
        return (root, query, criteriaBuilder) -> criteriaBuilder.or(
                criteriaBuilder.isTrue(criteriaBuilder.function(
                        "fts_match", Boolean.class,
                        root.get("searchVector"), criteriaBuilder.literal(q))),
                criteriaBuilder.greaterThan(criteriaBuilder.function(
                        "trgm_sim", Double.class,
                        root.get("searchText"), criteriaBuilder.literal(q)), 0.2));
    }

}

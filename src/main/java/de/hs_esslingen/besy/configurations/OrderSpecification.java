package de.hs_esslingen.besy.configurations;


import de.hs_esslingen.besy.models.Order;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {}

    public static <T> Specification<Order> contains(List<T> attributeList, String attributeName) {
        return (root, query, criteriaBuilder) -> {
            if (attributeList == null || attributeList.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return root.get(attributeName).in(attributeList);
        };
    }


    public static <T extends Comparable<? super T>> Specification<Order> isBetween(T min, T max, String attributeName) {
        return (root, query, criteriaBuilder) -> {
            if (min == null && max == null) {
                return criteriaBuilder.conjunction();
            } else if (min != null && max != null) {
                return criteriaBuilder.between(root.get(attributeName), min, max);
            } else if(min != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get(attributeName), min);
            } else if(max != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get(attributeName), max);
            } else{
                return criteriaBuilder.conjunction();
            }
        };
    }


}

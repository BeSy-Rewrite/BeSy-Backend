package de.hs_esslingen.besy.repositories;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.CostCenter;
import de.hs_esslingen.besy.models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CostCenterRepository costCenterRepository;

    private CostCenter costCenter1;
    private CostCenter costCenter2;
    private Order order1;
    private Order order2;

    @BeforeEach
    void setUp() {
        costCenter1 = new CostCenter();
        costCenter1.setId("CC-1");
        costCenter1.setName("Main CC");
        costCenter1 = costCenterRepository.save(costCenter1);

        costCenter2 = new CostCenter();
        costCenter2.setId("CC-2");
        costCenter2.setName("Secondary CC");
        costCenter2 = costCenterRepository.save(costCenter2);

        order1 = createOrder(costCenter1, "25", (short) 1, OrderStatus.IN_PROGRESS);
        order2 = createOrder(costCenter1, "25", (short) 3, OrderStatus.APPROVED);

        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @Test
    void should_find_top_order_by_cost_center_and_booking_year_ordered_by_auto_index_desc() {
        Order result = orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-1", "25");

        assertNotNull(result);
        assertEquals(order2.getId(), result.getId());
        assertEquals((short) 3, result.getAutoIndex());
    }

    @Test
    void should_return_null_when_no_order_matches_cost_center_and_booking_year() {
        Order result = orderRepository.findTopByPrimaryCostCenterIdAndBookingYearOrderByAutoIndexDesc("CC-2", "99");

        assertNull(result);
    }

    @Test
    void should_return_true_when_exists_by_id_and_status_not_matches() {
        boolean result = orderRepository.existsByIdAndStatusNot(order1.getId(), OrderStatus.COMPLETED);

        assertTrue(result);
    }

    @Test
    void should_return_false_when_exists_by_id_and_status_not_does_not_match() {
        boolean result = orderRepository.existsByIdAndStatusNot(order1.getId(), OrderStatus.IN_PROGRESS);

        assertFalse(result);
    }

    @Test
    void should_return_false_when_exists_by_id_and_status_not_for_non_existent_id() {
        boolean result = orderRepository.existsByIdAndStatusNot(9999L, OrderStatus.IN_PROGRESS);

        assertFalse(result);
    }

    private Order createOrder(CostCenter costCenter, String bookingYear, short autoIndex, OrderStatus status) {
        Order order = new Order();
        order.setPrimaryCostCenter(costCenter);
        order.setBookingYear(bookingYear);
        order.setAutoIndex(autoIndex);
        order.setStatus(status);
        order.setContentDescription("Test order");
        order.setCreatedDate(LocalDateTime.now());
        return order;
    }
}

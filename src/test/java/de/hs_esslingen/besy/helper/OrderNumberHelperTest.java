package de.hs_esslingen.besy.helper;

import de.hs_esslingen.besy.models.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderNumberHelperTest {

    private OrderNumberHelper orderNumberHelper;

    @BeforeEach
    void setUp() {
        orderNumberHelper = new OrderNumberHelper();
        ReflectionTestUtils.setField(orderNumberHelper, "orderNumberPrefix", "BE-");
        ReflectionTestUtils.setField(orderNumberHelper, "orderNumberSeparator", "-");
    }

    @Test
    void testGenerateOrderNumberWithValidOrder() {
        Order order = Order.builder()
                .primaryCostCenterId("123")
                .bookingYear("24")
                .autoIndex((short) 1)
                .build();

        String result = orderNumberHelper.generateOrderNumber(order);
        assertEquals("BE-123-24-001", result);
    }

    @Test
    void testGenerateOrderNumberWithNullOrder() {
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber(null));
    }

    @Test
    void testGenerateOrderNumberWithValidParameters() {
        String result = orderNumberHelper.generateOrderNumber("456", "25", (short) 42);
        assertEquals("BE-456-25-042", result);
    }

    @Test
    void testGenerateOrderNumberWithNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber(null, "24", (short) 1));
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber("123", null, (short) 1));
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber("123", "24", null));
    }

    @Test
    void testGenerateOrderNumberWithNullPrefixAndSeparator() {
        ReflectionTestUtils.setField(orderNumberHelper, "orderNumberPrefix", null);
        ReflectionTestUtils.setField(orderNumberHelper, "orderNumberSeparator", null);

        String result = orderNumberHelper.generateOrderNumber("123", "24", (short) 1);
        assertEquals("12324001", result);
    }

    @Test
    void testGenerateOrderNumberFromOrderWithNullPrimaryCostCenterId() {
        Order order = Order.builder()
                .primaryCostCenterId(null)
                .bookingYear("24")
                .autoIndex((short) 1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber(order));
    }

    @Test
    void testGenerateOrderNumberFromOrderWithNullBookingYear() {
        Order order = Order.builder()
                .primaryCostCenterId("123")
                .bookingYear(null)
                .autoIndex((short) 1)
                .build();
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber(order));
    }

    @Test
    void testGenerateOrderNumberFromOrderWithNullAutoIndex() {
        Order order = Order.builder()
                .primaryCostCenterId("123")
                .bookingYear("24")
                .autoIndex(null)
                .build();
        assertThrows(IllegalArgumentException.class, () -> orderNumberHelper.generateOrderNumber(order));
    }
}
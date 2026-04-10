package de.hs_esslingen.besy.helper;

import de.hs_esslingen.besy.models.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderNumberHelper {

    @Value("${order-number.prefix}")
    private String orderNumberPrefix;

    @Value("${order-number.separator}")
    private String orderNumberSeparator;

    public String generateOrderNumber(Order order) {
        return this.generateOrderNumber(order.getPrimaryCostCenterId(), order.getBookingYear(), order.getAutoIndex());
    }

    public String generateOrderNumber(String primaryCostCenterId, String bookingYear, Short autoIndex) {
        String[] orderNumberParts = {
                orderNumberPrefix + primaryCostCenterId,
                bookingYear,
                String.format("%03d", autoIndex)
        };

        return String.join(orderNumberSeparator, orderNumberParts);
    }
}

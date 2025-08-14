package de.hs_esslingen.besy.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {
    @Override
    public String convertToDatabaseColumn(OrderStatus orderStatus) {
        if(orderStatus == null) return null;
        return orderStatus.getDescription();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String s) {
        if(s == null) return null;

        for (OrderStatus value : OrderStatus.values()) {
            if (value.getDescription().equals(s)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + s);
    }
}

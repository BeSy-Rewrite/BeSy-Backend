package de.hs_esslingen.besy.mail;

import de.hs_esslingen.besy.enums.OrderStatus;

public record OrderStatusChangedEvent(long orderId, OrderStatus previousStatus, OrderStatus newStatus, long userId) {
}

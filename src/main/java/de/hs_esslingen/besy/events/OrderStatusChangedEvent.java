package de.hs_esslingen.besy.events;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;

public record OrderStatusChangedEvent(Order order, OrderStatus previousStatus, OrderStatus newStatus, User user) {
}

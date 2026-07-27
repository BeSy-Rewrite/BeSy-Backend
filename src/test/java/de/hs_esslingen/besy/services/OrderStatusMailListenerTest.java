package de.hs_esslingen.besy.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.events.OrderStatusChangedEvent;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.repositories.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderStatusMailListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private OrderStatusMailListener listener;

    @Test
    void should_delegate_relevant_status_changes_to_mail_service() {
        Order order = new Order();
        order.setId(1L);
        order.setPrimaryCostCenterId("CC-1");
        order.setBookingYear("25");

        User user = new User();
        user.setEmail("test@example.com");
        order.setOwner(user);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        listener.onOrderStatusChanged(
                new OrderStatusChangedEvent(order, OrderStatus.DEKAN_PENDING, OrderStatus.APPROVED, user));

        verify(mailService).sendOrderStatusChangeMail(order, OrderStatus.DEKAN_PENDING, OrderStatus.APPROVED, user);
    }

    @Test
    void should_ignore_status_changes_that_are_not_configured_for_mail() {
        Order order = new Order();
        order.setId(1L);
        User user = new User();
        user.setEmail("test@example.com");
        order.setOwner(user);

        listener.onOrderStatusChanged(
                new OrderStatusChangedEvent(order, OrderStatus.IN_PROGRESS, OrderStatus.COMPLETED, user));

        verify(orderRepository, never()).findById(any());
        verify(mailService, never()).sendOrderStatusChangeMail(any(), any(), any(), any());
    }
}

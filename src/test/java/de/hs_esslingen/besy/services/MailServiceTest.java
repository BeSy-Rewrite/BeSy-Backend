package de.hs_esslingen.besy.services;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.mail.MailService;
import de.hs_esslingen.besy.mail.MailTemplateRenderer;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    private MailTemplateRenderer templateRenderer;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(mailSender, orderService, userService, templateRenderer);
        ReflectionTestUtils.setField(mailService, "senderMail", "besy@it.hs-esslingen.de");
        ReflectionTestUtils.setField(mailService, "approvalMails",
                new String[] { "dekan@hs-esslingen.de", "tmp@hs-esslingen.de" });
    }

    @Test
    void should_send_order_status_change_mail_to_approval_recipients() {
        Order order = new Order();
        order.setId(42L);
        order.setPrimaryCostCenterId("CC-1");
        order.setBookingYear("25");
        order.setContentDescription("Notebook purchase");

        User user = new User();
        user.setEmail("jane.doe@hs-esslingen.de");
        user.setId(1L);

        mailService.sendOrderStatusChangeMail(order.getId(), OrderStatus.COMPLETED, OrderStatus.DEKAN_PENDING,
                user.getId());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("besy@it.hs-esslingen.de", message.getFrom());
        assertArrayEquals(new String[] { "dekan@hs-esslingen.de", "tmp@hs-esslingen.de" }, message.getTo());
        assertEquals("Order 42 changed to APPROVED", message.getSubject());
        assertTrue(message.getText().contains("Previous status: DEKAN_PENDING"));
        assertTrue(message.getText().contains("New status: APPROVED"));
    }
}

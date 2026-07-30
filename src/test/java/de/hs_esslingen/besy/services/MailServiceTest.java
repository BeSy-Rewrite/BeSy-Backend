package de.hs_esslingen.besy.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.mail.MailService;
import de.hs_esslingen.besy.mail.MailTemplateRenderer;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OrderService orderService;

    @Mock
    private UserService userService;

    @Mock
    private MailTemplateRenderer templateRenderer;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(mailSender, orderService, userService, templateRenderer);
        ReflectionTestUtils.setField(mailService, "senderMail", "besy@it.hs-esslingen.de");
        ReflectionTestUtils.setField(mailService, "approvalMails",
                new String[] { "dekan@hs-esslingen.de", "tmp@hs-esslingen.de" });
        ReflectionTestUtils.setField(mailService, "frontendUrl", "https://besy.example");
        // Approver werden bei DEKAN_PENDING benachrichtigt
        ReflectionTestUtils.setField(mailService, "notifyApproverStates",
                new String[] { OrderStatus.DEKAN_PENDING.name() });
        // Mindestens ein (anderer) User-Status, damit EnumSet.copyOf nicht auf leerer
        // Liste fehlschlägt
        ReflectionTestUtils.setField(mailService, "notifyUserStates",
                new String[] { OrderStatus.COMPLETED.name() });
    }

    @Test
    void should_send_order_status_change_mail_to_approval_recipients() throws Exception {
        User owner = new User();
        owner.setId(2L);
        owner.setEmail("owner@hs-esslingen.de");

        Order order = new Order();
        order.setId(42L);
        order.setOwner(owner);
        order.setPrimaryCostCenterId("CC-1");
        order.setBookingYear("25");
        order.setContentDescription("Notebook purchase");

        User user = new User();
        user.setId(1L);
        user.setEmail("jane.doe@hs-esslingen.de");

        when(orderService.getOrderById(42L)).thenReturn(Optional.of(order));
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(orderService.getOrderNumber(order)).thenReturn(Optional.of("B-2025-42"));
        when(templateRenderer.render(any(), anySet()))
                .thenReturn("<html>Previous status: DEKAN_PENDING; New status: APPROVED</html>");

        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendOrderStatusChangeMail(order.getId(), OrderStatus.COMPLETED, OrderStatus.DEKAN_PENDING,
                user.getId());

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());

        MimeMessage message = captor.getValue();

        assertEquals("besy@it.hs-esslingen.de",
                ((InternetAddress) message.getFrom()[0]).getAddress());

        Set<String> recipients = Arrays.stream(message.getRecipients(Message.RecipientType.TO))
                .map(a -> ((InternetAddress) a).getAddress())
                .collect(Collectors.toSet());
        assertEquals(Set.of("dekan@hs-esslingen.de", "tmp@hs-esslingen.de"), recipients);

        assertTrue(message.getSubject().startsWith("[BeSy] Bestellung B-2025-42 - neuer Status: "),
                "Unerwarteter Betreff: " + message.getSubject());
    }
}

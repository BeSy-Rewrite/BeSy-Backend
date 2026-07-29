package de.hs_esslingen.besy.mail;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;
import de.hs_esslingen.besy.services.OrderService;
import de.hs_esslingen.besy.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;
    private final OrderService orderService;
    private final UserService userService;
    private final MailTemplateRenderer templateRenderer;

    @Value("${besy.mail.notify-approver-states}")
    private String[] notifyApproverStates;

    @Value("${besy.mail.notify-user-states}")
    private String[] notifyUserStates;

    @Value("${besy-frontend-url}")
    private String frontendUrl;

    @Value("${besy.mail.sender}")
    private String senderMail;

    @Value("${besy.mail.approvals}")
    private String[] approvalMails;

    /**
     * Sends an email notification about an order status change to the appropriate
     * recipients.
     *
     * @param order          the order whose status has changed
     * @param previousStatus the previous status of the order
     * @param newStatus      the new status of the order
     * @param user           the user who triggered the status change (can be null)
     */
    @Transactional
    public void sendOrderStatusChangeMail(
            long orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            long userId) {
        if (!getNotifyUserStates().contains(newStatus)
                && (approvalMails == null || approvalMails.length == 0
                        || !getNotifyApproverStates().contains(newStatus))) {
            return;
        }

        Order order = orderService.getOrderById(orderId).orElseThrow();
        User user = userService.getUserById(userId).orElse(null);

        if (order.getOwner().getId() == user.getId() && getNotifyUserStates().contains(newStatus)) {
            // If the user is the owner of the order and the new status is in
            // notifyUserStates,
            // we don't need to send an email to the user.
            return;
        }

        String[] recipients = buildRecipients(order, newStatus, user);
        if (recipients.length == 0) {
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        try {
            helper.setFrom(senderMail);
            helper.setTo(recipients);
            helper.setSubject(buildSubject(order, newStatus));
            helper.setText(buildBody(order, previousStatus, newStatus), true);
            mailSender.send(message);
            log.info("Send state change mail for order {} to {}.", order.getId(), Arrays.toString(recipients));
        } catch (MessagingException e) {
            log.error("Failed to send state-change email to {}", Arrays.toString(recipients), e);
        }
    }

    private String[] buildRecipients(
            Order order,
            OrderStatus newStatus,
            User user) {
        HashSet<String> recipients = new HashSet<>();

        if (getNotifyUserStates().contains(newStatus)) {
            User owner = order.getOwner();
            if (owner != null && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                recipients.add(owner.getEmail());
            }
        }

        if (getNotifyApproverStates().contains(newStatus) && approvalMails != null) {
            Arrays.stream(approvalMails)
                    .filter(mail -> mail != null && !mail.isBlank())
                    .forEach(recipients::add);
        }

        if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
            recipients.remove(user.getEmail());
        }
        return recipients.toArray(String[]::new);
    }

    private String buildSubject(Order order, OrderStatus newStatus) {
        return "[BeSy] Bestellung " + orderService.getOrderNumber(order).orElse(String.valueOf(order.getId()))
                + " - neuer Status: "
                + statusLabel(newStatus);
    }

    private String buildBody(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        // Detect the "rejection" path: DEKAN_PENDING -> COMPLETED
        boolean isRejection = previousStatus == OrderStatus.DEKAN_PENDING
                && newStatus == OrderStatus.COMPLETED;

        OrderStatusPresentation p = OrderStatusPresentation.forStatus(newStatus);

        String headline = isRejection ? "Ihre Bestellung wurde nicht genehmigt" : p.headline;
        String introText = isRejection
                ? "Die folgende Bestellung wurde im Genehmigungsprozess abgelehnt."
                : p.introText;

        String orderNumber = orderService.getOrderNumber(order).orElse(String.valueOf(order.getId()));

        Map<String, String> values = new HashMap<>();
        values.put("STATUS_BADGE", p.badge);
        values.put("STATUS_BG_COLOR", p.bgColor);
        values.put("STATUS_TEXT_COLOR", p.textColor);
        values.put("HEADLINE", headline);
        values.put("INTRO_TEXT", introText);
        values.put("CTA_LABEL", p.ctaLabel);
        values.put("ORDER_NUMBER", orderNumber);
        values.put("CONTENT_DESCRIPTION", nullSafe(order.getContentDescription()));
        values.put("PRIMARY_COST_CENTER", nullSafe(order.getPrimaryCostCenterId()));
        values.put("BOOKING_YEAR", nullSafe(order.getBookingYear()));
        values.put("PREVIOUS_STATUS", statusLabel(previousStatus));
        values.put("NEW_STATUS", statusLabel(newStatus));
        values.put("ORDER_URL", frontendUrl + "/orders/" + order.getId());

        return templateRenderer.render(values);
    }

    private String statusLabel(OrderStatus status) {
        return status == null ? "-" : OrderStatusPresentation.forStatus(status).badge;
    }

    private String nullSafe(String value) {
        return Objects.toString(value, "-");
    }

    /**
     * Returns the set of order statuses for which approvers should be notified.
     *
     * @return a set of order statuses for which approvers should be notified
     */
    public Set<OrderStatus> getNotifyApproverStates() {
        if (notifyApproverStates == null) {
            return EnumSet.noneOf(OrderStatus.class);
        }
        return EnumSet.copyOf(EnumSet.allOf(OrderStatus.class).stream()
                .filter(status -> java.util.Arrays.asList(notifyApproverStates).contains(status.name()))
                .toList());
    }

    /**
     * Returns the set of order statuses for which users should be notified.
     *
     * @return a set of order statuses for which users should be notified
     */
    public Set<OrderStatus> getNotifyUserStates() {
        if (notifyUserStates == null) {
            return EnumSet.noneOf(OrderStatus.class);
        }
        return EnumSet.copyOf(EnumSet.allOf(OrderStatus.class).stream()
                .filter(status -> java.util.Arrays.asList(notifyUserStates).contains(status.name()))
                .toList());
    }

}

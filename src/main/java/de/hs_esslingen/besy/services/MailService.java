package de.hs_esslingen.besy.services;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import de.hs_esslingen.besy.enums.OrderStatus;
import de.hs_esslingen.besy.models.Order;
import de.hs_esslingen.besy.models.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final OrderService orderService;

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
    public void sendOrderStatusChangeMail(Order order, OrderStatus previousStatus, OrderStatus newStatus, User user) {
        if (!getNotifyUserStates().contains(newStatus) && (approvalMails == null || approvalMails.length == 0)) {
            return;
        }
        HashSet<String> recipients = new HashSet<>();

        if (getNotifyUserStates().contains(newStatus)) {
            User owner = order.getOwner();
            if (owner != null && owner.getEmail() != null && !owner.getEmail().isBlank()) {
                recipients.add(owner.getEmail());
            }
            if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                recipients.add(user.getEmail());
            }
        }

        if (getNotifyApproverStates().contains(newStatus) && approvalMails != null) {
            Arrays.stream(approvalMails)
                    .filter(mail -> mail != null && !mail.isBlank())
                    .forEach(recipients::add);
        }

        if (recipients.isEmpty()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderMail);
        message.setTo(recipients.toArray(String[]::new));
        message.setSubject(buildSubject(order, newStatus));
        message.setText(buildBody(order, previousStatus, newStatus));

        mailSender.send(message);
    }

    private String buildSubject(Order order, OrderStatus newStatus) {
        return "[BeSy] Bestellung " + orderService.getOrderNumber(order).orElse(order.getId().toString())
                + "hat den neuen Status: "
                + newStatus;
    }

    private String buildBody(Order order, OrderStatus previousStatus, OrderStatus newStatus) {
        return "Eine Bestellung wurde aktualisiert.\n\n"
                + "Bestellnummer: " + orderService.getOrderNumber(order).orElse(order.getId().toString()) + "\n"
                + "Primäre Kostenstelle: " + order.getPrimaryCostCenterId() + "\n"
                + "Buchungsjahr: " + order.getBookingYear() + "\n"
                + "Inhalt: " + order.getContentDescription() + "\n"
                + "Vorheriger Status: " + previousStatus + "\n"
                + "Neuer Status: " + newStatus + "\n"
                + "<a href=\"" + frontendUrl + "/orders/" + order.getId() + "\">Bestellung anzeigen</a>";
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

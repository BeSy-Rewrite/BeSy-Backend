package de.hs_esslingen.besy.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import de.hs_esslingen.besy.events.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusMailListener {

/*
besy.mail.notify-approver-states=DEKAN_PENDING
besy.mail.notify-user-states=APPROVED,REJECTED
*/


    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {

        if (!mailService.getNotifyApproverStates().contains(event.newStatus()) && !mailService.getNotifyUserStates().contains(event.newStatus())) {
            return;
        }

        mailService.sendOrderStatusChangeMail(
                event.order(),
                event.previousStatus(),
                event.newStatus(),
                event.user()
            );
    }

}

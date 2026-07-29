package de.hs_esslingen.besy.mail;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusMailListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {

        if (!mailService.getNotifyApproverStates().contains(event.newStatus())
                && !mailService.getNotifyUserStates().contains(event.newStatus())) {
            return;
        }

        mailService.sendOrderStatusChangeMail(
                event.orderId(),
                event.previousStatus(),
                event.newStatus(),
                event.userId());
    }

}

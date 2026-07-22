package nu.westlin.studiobooking.application.listener

import nu.westlin.studiobooking.application.NotificationService
import nu.westlin.studiobooking.domain.event.TrainingSessionEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TrainingSessionEventListener(
    private val notificationService: NotificationService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMemberPromotedFromWaitlist(event: TrainingSessionEvent.MemberPromotedFromWaitlist) {
        notificationService.notifyMemberPromotedFromWaitlist(
            memberId = event.memberId,
            sessionId = event.sessionId
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMemberAddedToWaitlist(event: TrainingSessionEvent.MemberAddedToWaitlist) {
        notificationService.notifyMemberAddedToWaitlist(
            memberId = event.memberId,
            sessionId = event.sessionId,
            position = event.waitlistPosition
        )
    }
}
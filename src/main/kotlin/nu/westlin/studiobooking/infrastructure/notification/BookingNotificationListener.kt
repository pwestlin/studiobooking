package nu.westlin.studiobooking.infrastructure.notification

import nu.westlin.studiobooking.domain.event.BookingCancelledEvent
import nu.westlin.studiobooking.domain.event.MemberBookedEvent
import nu.westlin.studiobooking.domain.event.TrainingSessionCancelledEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class BookingNotificationListener {
    private val log = LoggerFactory.getLogger(javaClass)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleMemberBooked(event: MemberBookedEvent) {
        log.info("Sent booking confirmation to member '${event.memberId.value}' for session '${event.sessionId.value}'")
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleBookingCancelled(event: BookingCancelledEvent) {
        log.info("Notifying member '${event.memberId.value}' that their booking for session '${event.sessionId.value}' was cancelled")
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleTrainingSessionCancelled(event: TrainingSessionCancelledEvent) {
        event.bookedMemberIds.forEach { memberId ->
            log.info("Notifying member '${memberId.value}' that training session '${event.sessionId.value}' has been cancelled")
        }
    }
}
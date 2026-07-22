package nu.westlin.studiobooking.infrastructure.notification

import nu.westlin.studiobooking.domain.event.MemberBookedEvent
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
}
package nu.westlin.studiobooking.domain.event

import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import java.time.Instant

sealed interface DomainEvent {
    val occurredAt: Instant
}

data class MemberBookedEvent(
    val sessionId: TrainingSessionId,
    val memberId: MemberId,
    override val occurredAt: Instant
) : DomainEvent

data class BookingCancelledEvent(
    val sessionId: TrainingSessionId,
    val memberId: MemberId,
    override val occurredAt: Instant
) : DomainEvent

data class TrainingSessionCancelledEvent(
    val sessionId: TrainingSessionId,
    override val occurredAt: Instant
) : DomainEvent
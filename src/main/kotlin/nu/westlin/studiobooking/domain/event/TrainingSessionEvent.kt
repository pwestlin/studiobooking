package nu.westlin.studiobooking.domain.event

import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import java.time.Instant

sealed interface TrainingSessionEvent : DomainEvent {
    val sessionId: TrainingSessionId

    data class MemberBooked(
        override val sessionId: TrainingSessionId,
        val memberId: MemberId,
        override val occurredAt: Instant
    ) : TrainingSessionEvent

    data class MemberAddedToWaitlist(
        override val sessionId: TrainingSessionId,
        val memberId: MemberId,
        val waitlistPosition: Int,
        override val occurredAt: Instant
    ) : TrainingSessionEvent

    data class MemberCancelled(
        override val sessionId: TrainingSessionId,
        val memberId: MemberId,
        override val occurredAt: Instant
    ) : TrainingSessionEvent

    data class MemberPromotedFromWaitlist(
        override val sessionId: TrainingSessionId,
        val memberId: MemberId,
        override val occurredAt: Instant
    ) : TrainingSessionEvent
}
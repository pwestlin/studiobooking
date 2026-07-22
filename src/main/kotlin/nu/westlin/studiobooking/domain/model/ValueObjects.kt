package nu.westlin.studiobooking.domain.model

import java.time.Instant
import java.util.*

@JvmInline
value class TrainingSessionId(val value: UUID) {
    companion object {
        fun new(): TrainingSessionId = TrainingSessionId(UUID.randomUUID())
    }
}

@JvmInline
value class MemberId(val value: UUID) {
    companion object {
        fun new(): MemberId = MemberId(UUID.randomUUID())
    }
}

@JvmInline
value class Capacity(val value: Int) {
    init {
        require(value > 0) { "Capacity must be greater than zero" }
    }
}

enum class TrainingSessionStatus {
    SCHEDULED,
    CANCELLED,
    COMPLETED
}

/**
 * Value Object representing an individual member's booking in a session.
 */
data class Booking(
    val memberId: MemberId,
    val bookedAt: Instant
)
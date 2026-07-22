package nu.westlin.studiobooking.domain.model

import java.util.UUID

@JvmInline
value class TrainingSessionId(val value: UUID) {
    companion object {
        fun new(): TrainingSessionId = TrainingSessionId(UUID.randomUUID())
    }
}
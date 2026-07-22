package nu.westlin.studiobooking.domain.model

import java.util.UUID

@JvmInline
value class MemberId(val value: UUID) {
    companion object {
        fun new(): MemberId = MemberId(UUID.randomUUID())
    }
}
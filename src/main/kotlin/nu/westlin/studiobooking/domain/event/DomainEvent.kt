package nu.westlin.studiobooking.domain.event

import java.time.Instant

/**
 * Marker interface for all domain events.
 */
sealed interface DomainEvent {
    val occurredAt: Instant
}
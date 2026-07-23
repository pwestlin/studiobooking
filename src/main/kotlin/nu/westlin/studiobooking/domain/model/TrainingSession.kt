package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.event.BookingCancelledEvent
import nu.westlin.studiobooking.domain.event.DomainEvent
import nu.westlin.studiobooking.domain.event.MemberBookedEvent
import nu.westlin.studiobooking.domain.event.TrainingSessionCancelledEvent
import nu.westlin.studiobooking.domain.exception.MemberAlreadyBookedException
import nu.westlin.studiobooking.domain.exception.TrainingSessionAlreadyCancelledException
import nu.westlin.studiobooking.domain.exception.TrainingSessionFullException
import java.time.Instant

/**
 * Aggregate Root representing a group fitness training session.
 * Protects invariants such as capacity limits, duplicate booking prevention, and status transitions.
 */
class TrainingSession(
    val id: TrainingSessionId,
    val name: String,
    val capacity: Capacity,
    val startTime: Instant,
    val endTime: Instant,
    status: TrainingSessionStatus,
    bookings: Set<Booking>
) {
    init {
        require(endTime.isAfter(startTime)) { "End time must be after start time" }
        require(bookings.size <= capacity.value) { "Initial bookings cannot exceed session capacity" }
    }

    var status: TrainingSessionStatus = status
        private set

    val bookings: Set<Booking>
        field: MutableSet<Booking> = bookings.toMutableSet()

    val domainEvents: List<DomainEvent>
        field: MutableList<DomainEvent> = mutableListOf()

    /**
     * Books a member for this session if capacity allows and session is active.
     */
    fun book(memberId: MemberId, now: Instant) {
        if (status != TrainingSessionStatus.SCHEDULED) {
            throw TrainingSessionAlreadyCancelledException(id)
        }
        if (bookings.size >= capacity.value) {
            throw TrainingSessionFullException(id)
        }
        if (bookings.any { it.memberId == memberId }) {
            throw MemberAlreadyBookedException(id, memberId)
        }

        bookings.add(Booking(memberId, now))
        domainEvents.add(MemberBookedEvent(id, memberId, now))
    }

    /**
     * Cancels an existing booking for a member.
     */
    fun cancelBooking(memberId: MemberId, now: Instant) {
        check(status == TrainingSessionStatus.SCHEDULED) { "Cannot cancel booking for a session with status $status" }
        val removed = bookings.removeIf { it.memberId == memberId }
        check(removed) { "Member $memberId does not have an active booking for session $id" }

        domainEvents.add(BookingCancelledEvent(id, memberId, now))
    }

    /**
     * Cancels the training session.
     */
    fun cancel(now: Instant) {
        check(status == TrainingSessionStatus.SCHEDULED) { "Session $id is already $status" }
        status = TrainingSessionStatus.CANCELLED
        val bookedMemberIds = bookings.map { it.memberId }.toSet()
        domainEvents.add(TrainingSessionCancelledEvent(id, bookedMemberIds, now))
    }

    /**
     * Clears domain events after they have been processed or published.
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }

    fun availableSeats(): Int = capacity.value - bookings.size

    fun isFullyBooked(): Boolean = bookings.size >= capacity.value

    companion object {
        /**
         * Factory function for creating a brand new [TrainingSession] aggregate.
         */
        fun new(
            name: String,
            capacity: Capacity,
            startTime: Instant,
            endTime: Instant
        ): TrainingSession = TrainingSession(
            id = TrainingSessionId.new(),
            name = name,
            capacity = capacity,
            startTime = startTime,
            endTime = endTime,
            status = TrainingSessionStatus.SCHEDULED,
            bookings = emptySet()
        )
    }
}
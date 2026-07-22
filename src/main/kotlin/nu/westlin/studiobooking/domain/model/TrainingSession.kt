package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.event.TrainingSessionEvent
import java.time.Instant

class TrainingSession(
    val id: TrainingSessionId,
    val title: String,
    val startTime: Instant,
    val capacity: Capacity,
    participants: List<MemberId> = emptyList(),
    waitlist: List<MemberId> = emptyList()
) {
    // Explicit Backing Fields for internal mutable lists exposed as read-only lists
    val participants: List<MemberId>
        field = participants.toMutableList()

    val waitlist: List<MemberId>
        field = waitlist.toMutableList()

    val domainEvents: List<TrainingSessionEvent>
        field = mutableListOf()

    fun clearDomainEvents() {
        domainEvents.clear()
    }

    /**
     * Books a slot for a member or adds them to the waitlist if full.
     */
    fun book(memberId: MemberId, now: Instant): BookingResult {
        if (participants.contains(memberId)) {
            return BookingResult.Failure.AlreadyBooked
        }
        if (waitlist.contains(memberId)) {
            return BookingResult.Failure.AlreadyInWaitlist
        }

        return if (participants.size < capacity.value) {
            participants.add(memberId)
            domainEvents.add(
                TrainingSessionEvent.MemberBooked(
                    sessionId = id,
                    memberId = memberId,
                    occurredAt = now
                )
            )
            BookingResult.BookedSuccessfully
        } else {
            waitlist.add(memberId)
            val position = waitlist.size
            domainEvents.add(
                TrainingSessionEvent.MemberAddedToWaitlist(
                    sessionId = id,
                    memberId = memberId,
                    waitlistPosition = position,
                    occurredAt = now
                )
            )
            BookingResult.AddedToWaitlist(position)
        }
    }
}
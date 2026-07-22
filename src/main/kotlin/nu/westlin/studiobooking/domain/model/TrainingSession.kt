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
    val participants: List<MemberId>
        field = participants.toMutableList()

    val waitlist: List<MemberId>
        field = waitlist.toMutableList()

    val domainEvents: List<TrainingSessionEvent>
        field = mutableListOf<TrainingSessionEvent>()

    fun clearDomainEvents() {
        domainEvents.clear()
    }

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

    /**
     * Cancels a member's booking or waitlist position.
     * If a participant cancels and waitlist is non-empty, automatically promotes the first waitlisted member.
     */
    fun cancel(memberId: MemberId, now: Instant): CancellationResult {
        if (participants.contains(memberId)) {
            participants.remove(memberId)
            domainEvents.add(
                TrainingSessionEvent.MemberCancelled(
                    sessionId = id,
                    memberId = memberId,
                    occurredAt = now
                )
            )

            return if (waitlist.isNotEmpty()) {
                val promotedMember = waitlist.removeAt(0)
                participants.add(promotedMember)
                domainEvents.add(
                    TrainingSessionEvent.MemberPromotedFromWaitlist(
                        sessionId = id,
                        memberId = promotedMember,
                        occurredAt = now
                    )
                )
                CancellationResult.CancelledAndPromotedFromWaitlist(promotedMember)
            } else {
                CancellationResult.CancelledSuccessfully
            }
        }

        if (waitlist.contains(memberId)) {
            waitlist.remove(memberId)
            domainEvents.add(
                TrainingSessionEvent.MemberCancelled(
                    sessionId = id,
                    memberId = memberId,
                    occurredAt = now
                )
            )
            return CancellationResult.CancelledSuccessfully
        }

        return CancellationResult.Failure.NotBooked
    }
}
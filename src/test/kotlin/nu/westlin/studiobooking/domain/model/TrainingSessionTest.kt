package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.event.MemberBookedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class TrainingSessionTest {

    private val now: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS)
    private val startTime: Instant = now.plus(1, ChronoUnit.HOURS)
    private val endTime: Instant = startTime.plus(1, ChronoUnit.HOURS)

    @Test
    fun `publish MemberBookedEvent when booking session`() {
        val session = TrainingSession.new(
            name = "Pass",
            capacity = Capacity(10),
            startTime = startTime,
            endTime = endTime
        )
        val memberId = MemberId.new()

        session.book(memberId, now)

        assertThat(session.domainEvents)
            .containsExactly(MemberBookedEvent(session.id, memberId, now))
    }

    @Test
    fun `clear domain events after publication`() {
        val session = TrainingSession.new(
            name = "Pass",
            capacity = Capacity(10),
            startTime = startTime,
            endTime = endTime
        )
        session.book(MemberId.new(), now)

        session.clearDomainEvents()

        assertThat(session.domainEvents).isEmpty()
    }
}
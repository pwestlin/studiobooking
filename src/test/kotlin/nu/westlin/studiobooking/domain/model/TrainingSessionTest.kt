package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.event.TrainingSessionEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class TrainingSessionTest {

    private val now = Instant.parse("2026-07-22T10:00:00Z")
    private val sessionId = TrainingSessionId.new()
    private val capacity = Capacity(2) // Max 2 deltagare för enkel testning

    private lateinit var session: TrainingSession

    @BeforeEach
    fun setUp() {
        session = TrainingSession(
            id = sessionId,
            title = "Bodypump 45m",
            startTime = now.plusSeconds(3600),
            capacity = capacity
        )
    }

    @Nested
    inner class Booking {

        @Test
        fun `should book member successfully when capacity is available`() {
            val memberId = MemberId.new()

            val result = session.book(memberId = memberId, now = now)

            assertEquals(BookingResult.BookedSuccessfully, result)
            assertEquals(1, session.participants.size)
            assertTrue(session.participants.contains(memberId))
            assertTrue(session.waitlist.isEmpty())
        }

        @Test
        fun `should add member to waitlist when capacity is full`() {
            val member1 = MemberId.new()
            val member2 = MemberId.new()
            val member3 = MemberId.new()

            // Fyll platserna (Kapacitet = 2)
            session.book(member1, now)
            session.book(member2, now)

            // Boka när det är fullt
            val result = session.book(member3, now)

            assertEquals(BookingResult.AddedToWaitlist(position = 1), result)
            assertEquals(2, session.participants.size)
            assertEquals(1, session.waitlist.size)
            assertEquals(member3, session.waitlist.first())
        }

        @Test
        fun `should maintain waitlist positions in order of booking`() {
            val member1 = MemberId.new()
            val member2 = MemberId.new()
            val memberWaitlist1 = MemberId.new()
            val memberWaitlist2 = MemberId.new()

            session.book(member1, now)
            session.book(member2, now)

            val result1 = session.book(memberWaitlist1, now)
            val result2 = session.book(memberWaitlist2, now)

            assertEquals(BookingResult.AddedToWaitlist(position = 1), result1)
            assertEquals(BookingResult.AddedToWaitlist(position = 2), result2)
            assertEquals(listOf(memberWaitlist1, memberWaitlist2), session.waitlist)
        }

        @Test
        fun `should fail to book when member is already booked`() {
            val memberId = MemberId.new()
            session.book(memberId, now)

            val result = session.book(memberId, now)

            assertEquals(BookingResult.Failure.AlreadyBooked, result)
            assertEquals(1, session.participants.size)
        }

        @Test
        fun `should fail to book when member is already in waitlist`() {
            val member1 = MemberId.new()
            val member2 = MemberId.new()
            val member3 = MemberId.new()

            session.book(member1, now)
            session.book(member2, now)
            session.book(member3, now) // Hamnar på väntelista

            val result = session.book(member3, now) // Försöker boka igen

            assertEquals(BookingResult.Failure.AlreadyInWaitlist, result)
            assertEquals(1, session.waitlist.size)
        }
    }

    @Nested
    inner class DomainEvents {

        @Test
        fun `should emit MemberBooked event on successful booking`() {
            val memberId = MemberId.new()

            session.book(memberId, now)

            assertEquals(1, session.domainEvents.size)
            val event = session.domainEvents.first()

            assertInstanceOf(TrainingSessionEvent.MemberBooked::class.java, event)
            val bookedEvent = event as TrainingSessionEvent.MemberBooked
            assertEquals(sessionId, bookedEvent.sessionId)
            assertEquals(memberId, bookedEvent.memberId)
            assertEquals(now, bookedEvent.occurredAt)
        }

        @Test
        fun `should emit MemberAddedToWaitlist event when booked to full session`() {
            val member1 = MemberId.new()
            val member2 = MemberId.new()
            val member3 = MemberId.new()

            session.book(member1, now)
            session.book(member2, now)
            session.clearDomainEvents() // Rensa tidigare events

            session.book(member3, now)

            assertEquals(1, session.domainEvents.size)
            val event = session.domainEvents.first()

            assertInstanceOf(TrainingSessionEvent.MemberAddedToWaitlist::class.java, event)
            val waitlistEvent = event as TrainingSessionEvent.MemberAddedToWaitlist
            assertEquals(sessionId, waitlistEvent.sessionId)
            assertEquals(member3, waitlistEvent.memberId)
            assertEquals(1, waitlistEvent.waitlistPosition)
            assertEquals(now, waitlistEvent.occurredAt)
        }

        @Test
        fun `should not emit events on failed booking attempts`() {
            val memberId = MemberId.new()
            session.book(memberId, now)
            session.clearDomainEvents()

            session.book(memberId, now) // Misslyckad dubbelbokning

            assertTrue(session.domainEvents.isEmpty())
        }

        @Test
        fun `should clear domain events when clearDomainEvents is invoked`() {
            val memberId = MemberId.new()
            session.book(memberId, now)

            session.clearDomainEvents()

            assertTrue(session.domainEvents.isEmpty())
        }
    }

    @Nested
        inner class Cancellation {

            @Test
            fun `should cancel booking when member has a confirmed slot`() {
                val memberId = MemberId.new()
                session.book(memberId, now)
                session.clearDomainEvents()

                val result = session.cancel(memberId, now)

                assertEquals(CancellationResult.CancelledSuccessfully, result)
                assertTrue(session.participants.isEmpty())
                assertEquals(1, session.domainEvents.size)

                val event = session.domainEvents.first()
                assertInstanceOf(TrainingSessionEvent.MemberCancelled::class.java, event)
            }

            @Test
            fun `should promote first member from waitlist when a participant cancels`() {
                val member1 = MemberId.new()
                val member2 = MemberId.new()
                val waitlistMember1 = MemberId.new()
                val waitlistMember2 = MemberId.new()

                session.book(member1, now)
                session.book(member2, now) // Fullt (Kapacitet = 2)
                session.book(waitlistMember1, now)
                session.book(waitlistMember2, now)
                session.clearDomainEvents()

                // Member 1 avbokar
                val result = session.cancel(member1, now)

                assertEquals(
                    CancellationResult.CancelledAndPromotedFromWaitlist(waitlistMember1),
                    result
                )
                // Deltagare ska nu vara member2 och waitlistMember1
                assertEquals(listOf(member2, waitlistMember1), session.participants)
                // Väntelistan ska nu bara ha waitlistMember2
                assertEquals(listOf(waitlistMember2), session.waitlist)

                // Verifiera att två events skapades: MemberCancelled och MemberPromotedFromWaitlist
                assertEquals(2, session.domainEvents.size)
                assertInstanceOf(TrainingSessionEvent.MemberCancelled::class.java, session.domainEvents[0])
                assertInstanceOf(TrainingSessionEvent.MemberPromotedFromWaitlist::class.java, session.domainEvents[1])

                val promotedEvent = session.domainEvents[1] as TrainingSessionEvent.MemberPromotedFromWaitlist
                assertEquals(waitlistMember1, promotedEvent.memberId)
            }

            @Test
            fun `should remove member from waitlist without promotion when waitlisted member cancels`() {
                val member1 = MemberId.new()
                val member2 = MemberId.new()
                val waitlistedMember = MemberId.new()

                session.book(member1, now)
                session.book(member2, now)
                session.book(waitlistedMember, now)
                session.clearDomainEvents()

                val result = session.cancel(waitlistedMember, now)

                assertEquals(CancellationResult.CancelledSuccessfully, result)
                assertEquals(2, session.participants.size)
                assertTrue(session.waitlist.isEmpty())
                assertEquals(1, session.domainEvents.size)
                assertInstanceOf(TrainingSessionEvent.MemberCancelled::class.java, session.domainEvents.first())
            }

            @Test
            fun `should fail to cancel when member is neither participant nor on waitlist`() {
                val unbookedMember = MemberId.new()

                val result = session.cancel(unbookedMember, now)

                assertEquals(CancellationResult.Failure.NotBooked, result)
                assertTrue(session.domainEvents.isEmpty())
            }
        }
}
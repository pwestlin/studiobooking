package nu.westlin.studiobooking.application

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.event.MemberBookedEvent
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.infrastructure.notification.BookingNotificationListener
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest
@Import(SharedTestcontainersConfiguration::class)
class BookTrainingSessionIntegrationTest @Autowired constructor(
    private val useCase: BookTrainingSessionUseCase,
    private val repository: TrainingSessionRepository,
    private val clock: Clock
) {

    @MockkBean
    private lateinit var notificationListener: BookingNotificationListener

    @Test
    fun `book training session, persist to database and handle event asynchronously`() {
        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Crosstraining",
            capacity = Capacity(12),
            startTime = now.plus(1, ChronoUnit.HOURS),
            endTime = now.plus(2, ChronoUnit.HOURS)
        )
        repository.save(session)

        val memberId = MemberId.new()

        // 1. Exekvera användningsfallet
        useCase.execute(BookTrainingSessionCommand(session.id, memberId))

        // 2. Verifiera tillståndet i databasen
        val updatedSession = repository.findById(session.id)
        assertThat(updatedSession).isNotNull
        assertThat(updatedSession?.bookings).hasSize(1)
        assertThat(updatedSession?.bookings?.first()?.memberId).isEqualTo(memberId)

        // 3. Verifiera att den asynkrona eventlyssnaren anropades efter commit
        await untilAsserted {
            verify(exactly = 1) {
                notificationListener.handleMemberBooked(
                    match<MemberBookedEvent> { event ->
                        event.sessionId == session.id && event.memberId == memberId
                    }
                )
            }
        }
    }
}
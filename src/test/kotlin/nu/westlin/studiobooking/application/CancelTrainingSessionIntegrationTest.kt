package nu.westlin.studiobooking.application

import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.verify
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.event.TrainingSessionCancelledEvent
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.infrastructure.notification.BookingNotificationListener
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import nu.westlin.studiobooking.test.isExactlyInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
class CancelTrainingSessionIntegrationTest @Autowired constructor(
    private val useCase: CancelTrainingSessionUseCase,
    private val repository: TrainingSessionRepository,
    private val clock: Clock
) {

    @MockkBean(relaxed = true)
    private lateinit var notificationListener: BookingNotificationListener

    @Test
    fun `cancel training session that does not exist`() {
        val sessionId = TrainingSessionId.new()
        assertThatThrownBy { useCase.execute(CancelTrainingSessionCommand(sessionId, Instant.now(clock))) }
            .isExactlyInstanceOf<IllegalArgumentException>()
            .hasMessage("Training session with ID ${sessionId.value} was not found")

        confirmVerified(notificationListener)
    }

    @Test
    fun `cancel training session`() {
        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Crosstraining",
            capacity = Capacity(12),
            startTime = now.plus(1, ChronoUnit.HOURS),
            endTime = now.plus(2, ChronoUnit.HOURS)
        )
        repository.save(session)

        // 1. Exekvera användningsfallet
        useCase.execute(CancelTrainingSessionCommand(session.id, Instant.now(clock)))

        // 2. Verifiera tillståndet i databasen
        val updatedSession = repository.findById(session.id)
        checkNotNull(updatedSession)
        assertThat(updatedSession.bookings).isEmpty()

        // 3. Verifiera att den asynkrona eventlyssnaren anropades efter commit
        await untilAsserted {
            verify(exactly = 1) {
                notificationListener.handleTrainingSessionCancelled(
                    match<TrainingSessionCancelledEvent> { event ->
                        event.sessionId == session.id && event.bookedMemberIds.isEmpty()
                    }
                )
            }
        }

    }
}
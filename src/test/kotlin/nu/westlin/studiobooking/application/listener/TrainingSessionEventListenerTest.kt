package nu.westlin.studiobooking.application.listener

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import nu.westlin.studiobooking.application.CancelTrainingSessionBookingUseCase
import nu.westlin.studiobooking.application.NotificationService
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.domain.repository.TrainingSessionRepository
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.Clock

@SpringBootTest
@Import(SharedTestcontainersConfiguration::class)
class TrainingSessionEventListenerIntegrationTest {

    @Autowired
    private lateinit var cancelBookingUseCase: CancelTrainingSessionBookingUseCase

    @Autowired
    private lateinit var repository: TrainingSessionRepository

    @Autowired
    private lateinit var clock: Clock

    @MockkBean(relaxed = true)
    private lateinit var notificationService: NotificationService

    @Test
    fun `should send notification when a member is promoted from waitlist on cancellation`() {
        // Arrange
        val now = clock.instant()
        val sessionId = TrainingSessionId.new()
        val member1 = MemberId.new()
        val waitlistMember = MemberId.new()

        val session = TrainingSession(
            id = sessionId,
            title = "Yoga",
            startTime = now.plusSeconds(3600),
            capacity = Capacity(1) // Kapacitet = 1
        )
        session.book(member1, now)
        session.book(waitlistMember, now) // Hamnar på väntelista
        session.clearDomainEvents()

        repository.save(session)

        // Act: Member 1 avbokar -> waitlistMember ska flyttas upp
        cancelBookingUseCase.execute(sessionId, member1)

        // Assert: Verifiera att notificationService anropades efter att transaktionen slutförts
        verify(exactly = 1) {
            notificationService.notifyMemberPromotedFromWaitlist(
                memberId = waitlistMember,
                sessionId = sessionId
            )
        }
    }
}
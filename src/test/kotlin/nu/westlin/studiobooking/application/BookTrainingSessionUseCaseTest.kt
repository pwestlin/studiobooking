package nu.westlin.studiobooking.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import nu.westlin.studiobooking.domain.MemberRepository
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.exception.TrainingSessionNotFoundException
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.test.isInstanceOf
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class BookTrainingSessionUseCaseTest {

    private val trainingSessionRepository: TrainingSessionRepository = mockk(relaxed = true)
    private val memberRepository: MemberRepository = mockk(relaxed = true)
    private val fixedInstant = Instant.parse("2026-06-01T10:00:00Z")
    private val clock: Clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"))

    private val useCase = BookTrainingSessionUseCase(trainingSessionRepository, memberRepository, clock)

    @Test
    fun `book training session successfully`() {
        val session = TrainingSession.new(
            name = "Padel & HIIT",
            capacity = Capacity(8),
            startTime = fixedInstant.plusSeconds(3600),
            endTime = fixedInstant.plusSeconds(7200)
        )
        val memberId = MemberId.new()
        val command = BookTrainingSessionCommand(
            sessionId = session.id,
            memberId = memberId
        )

        every { trainingSessionRepository.findById(session.id) } returns session

        useCase.execute(command)

        verify(exactly = 1) { trainingSessionRepository.save(session) }
    }

    @Test
    fun `throw exception when training session does not exist`() {
        val sessionId = TrainingSessionId.new()
        val command = BookTrainingSessionCommand(
            sessionId = sessionId,
            memberId = MemberId.new()
        )

        every { trainingSessionRepository.findById(sessionId) } returns null

        assertThatThrownBy { useCase.execute(command) }
            .isInstanceOf<TrainingSessionNotFoundException>()
            .hasMessage("Training session with id '$sessionId' was not found.")
    }
}
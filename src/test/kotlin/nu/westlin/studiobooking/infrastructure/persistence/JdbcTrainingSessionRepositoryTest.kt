package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.context.annotation.Import
import java.time.Instant

@DataJdbcTest
@Import(JdbcTrainingSessionRepository::class, SharedTestcontainersConfiguration::class)
class JdbcTrainingSessionRepositoryTest @Autowired constructor(
    private val repository: JdbcTrainingSessionRepository
) {

    @Test
    fun `should save and retrieve a training session with bookings and waitlist`() {
        // Arrange
        val sessionId = TrainingSessionId.new()
        val member1 = MemberId.new()
        val waitlistMember = MemberId.new()
        val now = Instant.now()

        val session = TrainingSession(
            id = sessionId,
            title = "Bodypump",
            startTime = now.plusSeconds(3600),
            capacity = Capacity(1)
        )
        session.book(member1, now)
        session.book(waitlistMember, now) // Hamnar på väntelistan

        // Act
        repository.save(session)
        val fetched = repository.findById(sessionId)

        // Assert
        assertThat(fetched).isNotNull
        assertThat(fetched!!.id).isEqualTo(sessionId)
        assertThat(fetched.title).isEqualTo("Bodypump")
        assertThat(fetched.bookings).containsExactly(member1)
        assertThat(fetched.waitlist).containsExactly(waitlistMember)
    }
}
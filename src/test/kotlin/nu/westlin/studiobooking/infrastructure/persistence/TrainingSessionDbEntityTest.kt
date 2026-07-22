package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionStatus
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.temporal.ChronoUnit

@DataJdbcTest
@Import(SharedTestcontainersConfiguration::class, JdbcTrainingSessionRepository::class)
class JdbcTrainingSessionRepositoryTest @Autowired constructor(
    private val repository: JdbcTrainingSessionRepository
) {

    @Test
    fun `save and find training session aggregate with bookings`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Spinning Express",
            capacity = Capacity(15),
            startTime = now.plusSeconds(3600),
            endTime = now.plusSeconds(7200)
        )
        val memberId = MemberId.new()
        session.book(memberId, now)

        repository.save(session)

        val retrieved = repository.findById(session.id)

        assertThat(retrieved).isNotNull
        assertThat(retrieved!!.id).isEqualTo(session.id)
        assertThat(retrieved.name).isEqualTo("Spinning Express")
        assertThat(retrieved.capacity).isEqualTo(Capacity(15))
        assertThat(retrieved.status).isEqualTo(TrainingSessionStatus.SCHEDULED)
        assertThat(retrieved.bookings).hasSize(1)

        val booking = retrieved.bookings.first()
        assertThat(booking.memberId).isEqualTo(memberId)
        assertThat(booking.bookedAt).isEqualTo(now)
    }

    @Test
    fun `update training session aggregate when booking is added`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Core & Mobility",
            capacity = Capacity(10),
            startTime = now.plusSeconds(3600),
            endTime = now.plusSeconds(7200)
        )
        repository.save(session)

        val existingSession = repository.findById(session.id)!!
        val memberId = MemberId.new()
        existingSession.book(memberId, now)

        repository.save(existingSession)

        val updated = repository.findById(session.id)
        assertThat(updated).isNotNull
        assertThat(updated!!.bookings).hasSize(1)
        assertThat(updated.bookings.first().memberId).isEqualTo(memberId)
    }

    @Test
    fun `delete training session removes session and cascading bookings`() {
        val now = Instant.now().truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "HIIT Blast",
            capacity = Capacity(12),
            startTime = now.plusSeconds(3600),
            endTime = now.plusSeconds(7200)
        )
        session.book(MemberId.new(), now)
        repository.save(session)

        repository.delete(session.id)

        val retrieved = repository.findById(session.id)
        assertThat(retrieved).isNull()
    }
}
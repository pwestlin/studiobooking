package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.model.Booking
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.domain.model.TrainingSessionStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Table("training_session")
data class TrainingSessionDbEntity(
    @Id val id: UUID,
    val name: String,
    val capacity: Int,
    val startTime: Instant,
    val endTime: Instant,
    val status: String,
    @Version val version: Long? = null,
    @MappedCollection(idColumn = "session_id")
    val bookings: Set<BookingDbEntity>
) {
    fun toDomain(): TrainingSession = TrainingSession(
        id = TrainingSessionId(id),
        name = name,
        capacity = Capacity(capacity),
        startTime = startTime,
        endTime = endTime,
        status = TrainingSessionStatus.valueOf(status),
        bookings = bookings.map { it.toDomain() }.toSet()
    )

    companion object {
        fun fromDomain(session: TrainingSession, version: Long? = null): TrainingSessionDbEntity =
            TrainingSessionDbEntity(
                id = session.id.value,
                name = session.name,
                capacity = session.capacity.value,
                startTime = session.startTime,
                endTime = session.endTime,
                status = session.status.name,
                version = version,
                bookings = session.bookings.map { BookingDbEntity.fromDomain(it) }.toSet()
            )
    }
}

@Table("booking")
data class BookingDbEntity(
    val memberId: UUID,
    val bookedAt: Instant
) {
    fun toDomain(): Booking = Booking(
        memberId = MemberId(memberId),
        bookedAt = bookedAt
    )

    companion object {
        fun fromDomain(booking: Booking): BookingDbEntity = BookingDbEntity(
            memberId = booking.memberId.value,
            bookedAt = booking.bookedAt
        )
    }
}

interface SpringDataTrainingSessionRepository : ListCrudRepository<TrainingSessionDbEntity, UUID>

@Repository
class JdbcTrainingSessionRepository(
    private val repository: SpringDataTrainingSessionRepository,
    private val eventPublisher: ApplicationEventPublisher
) : TrainingSessionRepository {

    override fun save(session: TrainingSession) {
        val existingVersion = repository.findById(session.id.value)
            .map { it.version }
            .orElse(null)

        val dbEntity = TrainingSessionDbEntity.fromDomain(session, version = existingVersion)
        repository.save(dbEntity)

        session.domainEvents.forEach(eventPublisher::publishEvent)
        session.clearDomainEvents()
    }

    override fun findById(id: TrainingSessionId): TrainingSession? =
        repository.findById(id.value).map { it.toDomain() }.orElse(null)

    override fun findAll(): List<TrainingSession> =
        repository.findAll().map { it.toDomain() }

    override fun delete(id: TrainingSessionId) {
        repository.deleteById(id.value)
    }
}
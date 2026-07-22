package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.domain.repository.TrainingSessionRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
internal class JdbcTrainingSessionRepository(
    private val springDataRepository: SpringDataTrainingSessionRepository,
    private val eventPublisher: ApplicationEventPublisher
) : TrainingSessionRepository {

    @Transactional(readOnly = true)
    override fun findById(id: TrainingSessionId): TrainingSession? {
        return springDataRepository.findById(id.value.toString())
            .map { it.toDomain() }
            .orElse(null)
    }

    @Transactional
    override fun save(session: TrainingSession): TrainingSession { // <-- Lägg till returtyp : TrainingSession
        // 1. Spara till databasen
        springDataRepository.save(session.toEntity())

        // 2. Publicera domän-events till Spring Application Context
        session.domainEvents.forEach { event ->
            eventPublisher.publishEvent(event)
        }

        // 3. Rensa events så de inte publiceras igen
        session.clearDomainEvents()

        // 4. Returnera aggregatet
        return session
    }

    // Mappning: Databas -> Domän
    private fun TrainingSessionEntity.toDomain(): TrainingSession {
        return TrainingSession.reconstitute(
            id = TrainingSessionId(id),
            title = title,
            capacity = Capacity(maxCapacity),
            startTime = startTime,
            endTime = endTime,
            initialBookings = bookings.map { MemberId(it.memberId) },
            initialWaitlist = waitlist.map { MemberId(it.memberId) }
        )
    }

    // Mappning: Domän -> Databas
    private fun TrainingSession.toEntity(): TrainingSessionEntity {
        val regularEntities = bookings.map { memberId ->
            BookingEntity(
                id = UUID.randomUUID().toString(),
                memberId = memberId.value.toString(),
                bookedAt = Instant.now(), // Eller bevara ursprungligt bokningsdatum om domänen spårar det
                type = "REGULAR",
                waitlistPosition = null
            )
        }

        val waitlistEntities = waitlist.mapIndexed { index, memberId ->
            BookingEntity(
                id = UUID.randomUUID().toString(),
                memberId = memberId.value.toString(),
                bookedAt = Instant.now(),
                type = "WAITLIST",
                waitlistPosition = index + 1
            )
        }

        return TrainingSessionEntity(
            id = id.value.toString(),
            title = title,
            startTime = startTime,
            capacity = capacity.value,
            bookings = (regularEntities + waitlistEntities).toSet()
        )
    }
}
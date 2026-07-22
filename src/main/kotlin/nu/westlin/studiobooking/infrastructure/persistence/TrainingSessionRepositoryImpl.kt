package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.domain.repository.TrainingSessionRepository
import org.springframework.stereotype.Repository

@Repository
class TrainingSessionRepositoryImpl(
    private val springDataRepository: SpringDataTrainingSessionRepository
) : TrainingSessionRepository {

    override fun findById(id: TrainingSessionId): TrainingSession? {
        return springDataRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun save(session: TrainingSession): TrainingSession {
        val entity = session.toEntity()
        val savedEntity = springDataRepository.save(entity)
        return savedEntity.toDomain()
    }

    private fun TrainingSessionEntity.toDomain(): TrainingSession {
        return TrainingSession(
            id = TrainingSessionId(id),
            title = title,
            startTime = startTime,
            capacity = Capacity(capacity),
            participants = participants.map { MemberId(it) },
            waitlist = waitlist.map { MemberId(it) }
        )
    }

    private fun TrainingSession.toEntity(): TrainingSessionEntity {
        return TrainingSessionEntity(
            id = id.value,
            title = title,
            startTime = startTime,
            capacity = capacity.value,
            participants = participants.map { it.value },
            waitlist = waitlist.map { it.value }
        )
    }
}
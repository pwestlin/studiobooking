package nu.westlin.studiobooking.application

import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class CreateTrainingSessionCommand(
    val name: String,
    val capacity: Capacity,
    val startTime: Instant,
    val endTime: Instant
)

/**
 * Use case for creating and persisting a new training session.
 */
@Service
class CreateTrainingSessionUseCase(
    private val repository: TrainingSessionRepository
) {
    @Transactional
    fun execute(command: CreateTrainingSessionCommand): TrainingSessionId {
        val session = TrainingSession.new(
            name = command.name,
            capacity = command.capacity,
            startTime = command.startTime,
            endTime = command.endTime
        )

        repository.save(session)
        return session.id
    }
}
package nu.westlin.studiobooking.application

import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

data class CancelTrainingSessionCommand(
    val sessionId: TrainingSessionId,
    val now: Instant
)

/**
 * Use case for cancelling an entire training session.
 */
@Service
class CancelTrainingSessionUseCase(
    private val repository: TrainingSessionRepository
) {
    @Transactional
    fun execute(command: CancelTrainingSessionCommand) {
        val session = repository.findById(command.sessionId)
            ?: throw IllegalArgumentException("Training session with ID ${command.sessionId.value} was not found")

        session.cancel(now = command.now)
        repository.save(session)
    }
}
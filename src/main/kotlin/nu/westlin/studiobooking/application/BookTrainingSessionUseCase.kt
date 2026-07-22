package nu.westlin.studiobooking.application

import nu.westlin.studiobooking.domain.model.BookingResult
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.domain.repository.TrainingSessionRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class BookTrainingSessionUseCase(
    private val repository: TrainingSessionRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val clock: Clock
) {

    @Transactional
    fun execute(sessionId: TrainingSessionId, memberId: MemberId): BookingResult {
        val session = repository.findById(sessionId)
            ?: throw IllegalArgumentException("Training session $sessionId not found")

        val result = session.book(memberId = memberId, now = clock.instant())

        if (result !is BookingResult.Failure) {
            repository.save(session)
            session.domainEvents.forEach(eventPublisher::publishEvent)
            session.clearDomainEvents()
        }

        return result
    }
}
package nu.westlin.studiobooking.application

import nu.westlin.studiobooking.domain.MemberRepository
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.exception.MemberNotFoundException
import nu.westlin.studiobooking.domain.exception.TrainingSessionNotFoundException
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class BookTrainingSessionCommand(
    val sessionId: TrainingSessionId,
    val memberId: MemberId
)

/**
 * Use case for booking a member to a scheduled training session.
 */
@Service
class BookTrainingSessionUseCase(
    private val repository: TrainingSessionRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock
) {

    @Transactional
    fun execute(command: BookTrainingSessionCommand) {
        val member = memberRepository.findById(command.memberId)
            ?: throw MemberNotFoundException(command.memberId)

        member.ensureCanBook()

        val session = repository.findById(command.sessionId)
            ?: throw TrainingSessionNotFoundException(command.sessionId)

        val now = clock.instant()
        session.book(memberId = command.memberId, now = now)

        repository.save(session)
    }
}
package nu.westlin.studiobooking.infrastructure.rest

import nu.westlin.studiobooking.application.BookTrainingSessionCommand
import nu.westlin.studiobooking.application.BookTrainingSessionUseCase
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.*

data class BookTrainingSessionRequest(
    val memberId: UUID
)

@RestController
@RequestMapping("/api/training-sessions")
class BookTrainingSessionController(
    private val useCase: BookTrainingSessionUseCase
) {

    @PostMapping("/{sessionId}/bookings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun book(
        @PathVariable sessionId: UUID,
        @RequestBody request: BookTrainingSessionRequest
    ) {
        useCase.execute(
            BookTrainingSessionCommand(
                sessionId = TrainingSessionId(sessionId),
                memberId = MemberId(request.memberId)
            )
        )
    }
}
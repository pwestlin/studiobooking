package nu.westlin.studiobooking.infrastructure.rest

import nu.westlin.studiobooking.application.BookTrainingSessionUseCase
import nu.westlin.studiobooking.domain.model.BookingResult
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import nu.westlin.studiobooking.infrastructure.rest.dto.BookSessionRequest
import nu.westlin.studiobooking.infrastructure.rest.dto.BookSessionResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/training-sessions")
class TrainingSessionController(
    private val bookTrainingSessionUseCase: BookTrainingSessionUseCase
) {

    @PostMapping("/{id}/bookings")
    fun bookSession(
        @PathVariable id: UUID,
        @RequestBody request: BookSessionRequest
    ): ResponseEntity<BookSessionResponse> {
        val result = bookTrainingSessionUseCase.execute(
            sessionId = TrainingSessionId(id),
            memberId = MemberId(request.memberId)
        )

        return when (result) {
            is BookingResult.BookedSuccessfully ->
                ResponseEntity.ok(BookSessionResponse(status = "CONFIRMED"))

            is BookingResult.AddedToWaitlist ->
                ResponseEntity.ok(BookSessionResponse(status = "WAITLISTED", waitlistPosition = result.position))

            is BookingResult.Failure.AlreadyBooked ->
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(BookSessionResponse(status = "ALREADY_BOOKED"))

            is BookingResult.Failure.AlreadyInWaitlist ->
                ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(BookSessionResponse(status = "ALREADY_IN_WAITLIST"))
        }
    }
}
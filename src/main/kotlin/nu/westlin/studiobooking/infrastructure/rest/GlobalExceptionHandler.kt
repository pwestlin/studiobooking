package nu.westlin.studiobooking.infrastructure.rest

import nu.westlin.studiobooking.domain.exception.DomainException
import nu.westlin.studiobooking.domain.exception.MemberAlreadyBookedException
import nu.westlin.studiobooking.domain.exception.TrainingSessionAlreadyCancelledException
import nu.westlin.studiobooking.domain.exception.TrainingSessionFullException
import nu.westlin.studiobooking.domain.exception.TrainingSessionNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    fun handleTrainingSessionNotFound(ex: TrainingSessionNotFoundException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.message ?: "Not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail)
    }

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ProblemDetail> {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Invalid argument"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail)
    }

    @ExceptionHandler
    fun handleTrainingSessionFull(ex: TrainingSessionFullException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.message ?: "Training session ${ex.sessionId} is full"
        )
        problemDetail.title = "Training Session Full"
        problemDetail.type = errorType("session-full")
        problemDetail.setProperty("sessionId", ex.sessionId)
        return problemDetail
    }

    @ExceptionHandler
    fun handleMemberAlreadyBookedException(ex: MemberAlreadyBookedException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.message ?: "Member is already booked on session"
        )
        problemDetail.title = "Training Session Full"
        problemDetail.type = URI.create("/errors/already-booked")
        problemDetail.setProperty("sessionId", ex.sessionId)
        return problemDetail
    }

    @ExceptionHandler
    fun handleTrainingSessionAlreadyCancelled(ex: TrainingSessionAlreadyCancelledException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Training session is already cancelled"
        )
        problemDetail.title = "Training Session Cancelled"
        problemDetail.type = URI.create("/errors/session-cancelled")
        problemDetail.setProperty("sessionId", ex.sessionId)
        return problemDetail
    }

    @ExceptionHandler
    fun handleGenericDomainException(ex: DomainException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.message ?: "Domain validation failed"
        )
        problemDetail.title = "Domain Rule Violation"
        return problemDetail
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(ex: Exception): ResponseEntity<String> {
        // Detta tvingar fram stacktracen i dina testloggar direkt när det smäller!
        logger.error("Test request failed with exception:", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.message)
    }

    private fun errorType(path: String): URI {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/errors/$path")
            .build()
            .toUri()
    }
}
package nu.westlin.studiobooking.infrastructure.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import nu.westlin.studiobooking.application.BookTrainingSessionCommand
import nu.westlin.studiobooking.application.BookTrainingSessionUseCase
import nu.westlin.studiobooking.domain.exception.TrainingSessionNotFoundException
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.*

@WebMvcTest(BookTrainingSessionController::class)
@Import(GlobalExceptionHandler::class)
@AutoConfigureRestTestClient
class BookTrainingSessionControllerTest(
    @Autowired private val restTestClient: RestTestClient
) {

    @MockkBean
    private lateinit var useCase: BookTrainingSessionUseCase

    @Test
    fun `book training session successfully`() {
        val sessionId = UUID.randomUUID()
        val memberId = UUID.randomUUID()

        every { useCase.execute(any()) } returns Unit

        restTestClient.post()
            .uri("/api/training-sessions/$sessionId/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BookTrainingSessionRequest(memberId = memberId))
            .exchange()
            .expectStatus().isNoContent

        verify(exactly = 1) {
            useCase.execute(
                BookTrainingSessionCommand(
                    sessionId = TrainingSessionId(sessionId),
                    memberId = MemberId(memberId)
                )
            )
        }
    }

    @Test
    fun `return 404 not found when training session does not exist`() {
        val sessionId = UUID.randomUUID()
        val memberId = UUID.randomUUID()

        every { useCase.execute(any()) } throws TrainingSessionNotFoundException(TrainingSessionId(sessionId))

        restTestClient.post()
            .uri("/api/training-sessions/$sessionId/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BookTrainingSessionRequest(memberId = memberId))
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.detail").isEqualTo("Training session with id '$sessionId' was not found.")
    }
}
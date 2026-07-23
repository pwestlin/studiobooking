package nu.westlin.studiobooking.infrastructure.rest

import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.test.web.servlet.client.expectBody
import java.net.URI
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(SharedTestcontainersConfiguration::class)
class BookTrainingSessionControllerErrorHandlingIntegrationTest @Autowired constructor(
    private val restTestClient: RestTestClient,
    private val repository: TrainingSessionRepository,
    private val clock: Clock,
    @LocalServerPort private val port: Int
) {

    @Test
    fun `return 409 conflict problem detail when training session is full`() {
        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)

        // Skapa en session med kapacitet 1 och fyll den direkt
        val session = TrainingSession.new(
            name = "Small Group Yoga",
            capacity = Capacity(1),
            startTime = now.plus(1, ChronoUnit.HOURS),
            endTime = now.plus(2, ChronoUnit.HOURS)
        )
        val existingMember = MemberId.new()
        session.book(existingMember, now)
        repository.save(session)

        // Försök göra en andra bokning via REST API
        val newMember = MemberId.new()
        val requestBody = BookSessionRequestDto(memberId = newMember.value)

        restTestClient.post()
            .uri("/api/training-sessions/{id}/bookings", session.id.value)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody<ProblemDetail>().value { problemDetail ->
                requireNotNull(problemDetail)
                assertThat(problemDetail.title).isEqualTo("Training Session Full")
                assertThat(problemDetail.status).isEqualTo(409)
                assertThat(problemDetail.detail).isEqualTo("Training session '${session.id}' is full")
                assertThat(problemDetail.type).isEqualTo(URI("http://localhost:$port/errors/session-full"))
                assertThat(problemDetail.properties).containsExactlyEntriesOf(mapOf("sessionId" to session.id.toString()))
            }
    }
}
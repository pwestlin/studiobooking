package nu.westlin.studiobooking.infrastructure.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import nu.westlin.studiobooking.domain.MemberRepository
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.event.MemberBookedEvent
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.Member
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.MemberStatus
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.infrastructure.notification.BookingNotificationListener
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(SharedTestcontainersConfiguration::class)
class BookTrainingSessionControllerIntegrationTest @Autowired constructor(
    private val restTestClient: RestTestClient,
    private val sessionRepository: TrainingSessionRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock
) {

    @MockkBean(relaxed = true)
    private lateinit var notificationListener: BookingNotificationListener

    @Test
    fun `book training session via HTTP endpoint and handle event asynchronously`() {
        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Crosstraining",
            capacity = Capacity(12),
            startTime = now.plus(1, ChronoUnit.HOURS),
            endTime = now.plus(2, ChronoUnit.HOURS)
        )
        sessionRepository.save(session)

        val member = Member(
            id = MemberId.new(),
            name = "Foo Bar",
            status = MemberStatus.ACTIVE
        )
        memberRepository.save(member)
        val requestBody = BookSessionRequestDto(memberId = member.id.value)

        // 1. Skicka HTTP-anrop mot systemgränsen
        restTestClient.post()
            .uri("/api/training-sessions/{id}/bookings", session.id.value)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isNoContent
            .expectBody().isEmpty

        // 2. Verifiera tillståndsändringen i databasen
        val updatedSession = sessionRepository.findById(session.id)
        assertThat(updatedSession).isNotNull
        assertThat(updatedSession?.bookings).hasSize(1)
        assertThat(updatedSession?.bookings?.first()?.memberId).isEqualTo(member.id)

        // 3. Verifiera att den asynkrona eventlyssnaren exekverades i bakgrunden
        await untilAsserted {
            verify(exactly = 1) {
                notificationListener.handleMemberBooked(
                    match<MemberBookedEvent> { event ->
                        event.sessionId == session.id && event.memberId == member.id
                    }
                )
            }
        }
    }
}

data class BookSessionRequestDto(
    val memberId: UUID
)
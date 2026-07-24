package nu.westlin.studiobooking.infrastructure.rest

import nu.westlin.studiobooking.domain.MemberRepository
import nu.westlin.studiobooking.domain.TrainingSessionRepository
import nu.westlin.studiobooking.domain.model.Capacity
import nu.westlin.studiobooking.domain.model.Member
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.MemberStatus
import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.test.web.servlet.client.RestTestClient
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(SharedTestcontainersConfiguration::class)
class BookTrainingSessionUseCaseIntegrationTest @Autowired constructor(
    private val restTestClient: RestTestClient,
    private val sessionRepository: TrainingSessionRepository,
    private val memberRepository: MemberRepository,
    private val jdbcClient: JdbcClient,
    private val clock: Clock
) {

    @BeforeEach
    fun `clean up database`() {
        jdbcClient.sql("truncate event_publication").update()
    }

    @Test
    fun `publish event to outbox table and complete execution asynchronously`() {
        val member = Member(
            id = MemberId.new(),
            name = "Foo Bar",
            status = MemberStatus.ACTIVE
        )
        memberRepository.save(member)

        val now = Instant.now(clock).truncatedTo(ChronoUnit.MICROS)
        val session = TrainingSession.new(
            name = "Spinning",
            capacity = Capacity(10),
            startTime = now.plus(1, ChronoUnit.HOURS),
            endTime = now.plus(2, ChronoUnit.HOURS)
        )
        sessionRepository.save(session)

        val memberId = member.id
        val requestBody = BookSessionRequestDto(memberId = memberId.value)

        // 1. Skicka anrop via systemgränsen
        restTestClient.post()
            .uri("/api/training-sessions/{id}/bookings", session.id.value)
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .exchange()
            .expectStatus().isNoContent

        // 2. Verifiera i event_publication-tabellen att eventet registrerades och slutfördes
        await untilAsserted {
            val completedCount = jdbcClient.sql(
                """
                SELECT count(*) 
                FROM event_publication 
                WHERE event_type LIKE '%MemberBookedEvent%' 
                  AND completion_date IS NOT NULL
                """
            )
                .query(Int::class.java)
                .single()

            assertThat(completedCount).isEqualTo(1)
        }
    }
}

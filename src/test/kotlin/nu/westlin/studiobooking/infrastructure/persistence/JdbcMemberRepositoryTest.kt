package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.model.Member
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.MemberStatus
import nu.westlin.studiobooking.test.SharedTestcontainersConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest
import org.springframework.context.annotation.Import

@DataJdbcTest
@Import(JdbcMemberRepository::class, SharedTestcontainersConfiguration::class)
class JdbcMemberRepositoryTest {

    @Autowired
    private lateinit var repository: JdbcMemberRepository

    @Test
    fun `save and find member by id`() {
        val member = Member(
            id = MemberId.new(),
            name = "Kalle Anka",
            status = MemberStatus.ACTIVE
        )

        repository.save(member)

        val fetchedMember = repository.findById(member.id)

        assertThat(fetchedMember).isEqualTo(member)
    }

    @Test
    fun `update existing member status`() {
        val member = Member(
            id = MemberId.new(),
            name = "Kalle Anka",
            status = MemberStatus.UNPAID
        )
        repository.save(member)

        val updatedMember = member.copy(status = MemberStatus.ACTIVE)
        repository.save(updatedMember)

        val fetchedMember = repository.findById(member.id)

        assertThat(fetchedMember?.status).isEqualTo(MemberStatus.ACTIVE)
    }

    @Test
    fun `return null when member is not found`() {
        val fetchedMember = repository.findById(MemberId.new())

        assertThat(fetchedMember).isNull()
    }
}
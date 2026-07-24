package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.exception.MemberCannotBookException
import nu.westlin.studiobooking.test.isInstanceOf
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MemberTest {

    @Test
    fun `active member is allowed to book session`() {
        val member = Member(MemberId.new(), "Anna", MemberStatus.ACTIVE)

        assertThatCode { member.ensureCanBook() }
            .doesNotThrowAnyException()
    }

    @Test
    fun `unpaid member throws exception when attempting to book`() {
        val member = Member(MemberId.new(), "Björn", MemberStatus.UNPAID)

        assertThatThrownBy { member.ensureCanBook() }
            .isInstanceOf<MemberCannotBookException>()
    }

    @Test
    fun `inactive member throws exception when attempting to book`() {
        val member = Member(MemberId.new(), "Cecilia", MemberStatus.INACTIVE)

        assertThatThrownBy { member.ensureCanBook() }
            .isInstanceOf<MemberCannotBookException>()
    }
}
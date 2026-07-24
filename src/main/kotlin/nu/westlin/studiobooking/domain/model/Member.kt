package nu.westlin.studiobooking.domain.model

import nu.westlin.studiobooking.domain.exception.MemberCannotBookException

enum class MemberStatus {
    ACTIVE,
    INACTIVE,
    UNPAID;
}

data class Member(
    val id: MemberId,
    val name: String,
    val status: MemberStatus
) {

    fun ensureCanBook() {
        if (status != MemberStatus.ACTIVE) {
            throw MemberCannotBookException(id, status)
        }
    }
}
package nu.westlin.studiobooking.domain

import nu.westlin.studiobooking.domain.model.Member
import nu.westlin.studiobooking.domain.model.MemberId

interface MemberRepository {
    fun findById(id: MemberId): Member?
    fun save(member: Member)
}
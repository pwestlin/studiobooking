package nu.westlin.studiobooking.infrastructure.persistence

import nu.westlin.studiobooking.domain.MemberRepository
import nu.westlin.studiobooking.domain.model.Member
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.MemberStatus
import org.springframework.stereotype.Repository

@Repository
class JdbcMemberRepository(
    private val springDataMemberRepository: SpringDataMemberRepository
) : MemberRepository {

    override fun findById(id: MemberId): Member? {
        return springDataMemberRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }

    override fun save(member: Member) {
        val existingEntity = springDataMemberRepository.findById(member.id.value).orElse(null)
        val entityToSave = member.toEntity(existingVersion = existingEntity?.version)
        springDataMemberRepository.save(entityToSave)
    }

    private fun MemberEntity.toDomain(): Member = Member(
        id = MemberId(id),
        name = name,
        status = MemberStatus.valueOf(status)
    )

    private fun Member.toEntity(existingVersion: Long?): MemberEntity = MemberEntity(
        id = id.value,
        name = name,
        status = status.name,
        version = existingVersion
    )
}
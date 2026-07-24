package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.repository.ListCrudRepository
import java.util.*

interface SpringDataMemberRepository : ListCrudRepository<MemberEntity, UUID>
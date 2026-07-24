package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("member")
data class MemberEntity(
    @Id val id: UUID,
    val name: String,
    val status: String,
    @Version val version: Long? = null
)
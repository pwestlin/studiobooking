package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("training_sessions")
data class TrainingSessionEntity(
    @Id
    val id: UUID,
    val title: String,
    val startTime: Instant,
    val capacity: Int,
    val participants: List<UUID>,
    val waitlist: List<UUID>
)
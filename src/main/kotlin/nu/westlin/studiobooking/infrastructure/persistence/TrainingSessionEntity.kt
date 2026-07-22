package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("training_session")
data class TrainingSessionEntity(
    @Id
    val id: String,
    val title: String,
    val startTime: Instant,
    val capacity: Int,
    @MappedCollection(idColumn = "training_session_id")
    val bookings: Set<BookingEntity> = emptySet()
)

@Table("training_session_booking")
data class BookingEntity(
    @Id
    val id: String,
    val memberId: String,
    val bookedAt: Instant,
    val type: String, // "REGULAR" eller "WAITLIST"
    val waitlistPosition: Int?
)
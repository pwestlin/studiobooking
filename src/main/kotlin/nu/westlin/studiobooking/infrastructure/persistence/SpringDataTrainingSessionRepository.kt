package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.repository.ListCrudRepository
import java.util.UUID

interface SpringDataTrainingSessionRepository : ListCrudRepository<TrainingSessionEntity, UUID>
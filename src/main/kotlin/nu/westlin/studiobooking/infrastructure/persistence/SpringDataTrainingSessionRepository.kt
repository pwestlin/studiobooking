package nu.westlin.studiobooking.infrastructure.persistence

import org.springframework.data.repository.CrudRepository

internal interface SpringDataTrainingSessionRepository : CrudRepository<TrainingSessionEntity, String>
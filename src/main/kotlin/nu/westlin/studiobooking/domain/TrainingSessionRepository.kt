package nu.westlin.studiobooking.domain

import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId

/**
 * Domain repository port for managing [TrainingSession] aggregate root persistence.
 */
interface TrainingSessionRepository {
    fun save(session: TrainingSession)
    fun findById(id: TrainingSessionId): TrainingSession?
    fun findAll(): List<TrainingSession>
    fun delete(id: TrainingSessionId)
}
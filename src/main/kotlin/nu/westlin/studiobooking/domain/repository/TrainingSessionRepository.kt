package nu.westlin.studiobooking.domain.repository

import nu.westlin.studiobooking.domain.model.TrainingSession
import nu.westlin.studiobooking.domain.model.TrainingSessionId

interface TrainingSessionRepository {
    fun findById(id: TrainingSessionId): TrainingSession?
    fun save(session: TrainingSession): TrainingSession
}
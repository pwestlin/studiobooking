package nu.westlin.studiobooking.domain

import nu.westlin.studiobooking.domain.model.TrainingSessionId

sealed class DomainException(message: String) : RuntimeException(message)

class TrainingSessionNotFoundException(id: TrainingSessionId) :
    DomainException("Training session with id '${id.value}' was not found.")
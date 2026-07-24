package nu.westlin.studiobooking.domain.exception

import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.MemberStatus
import nu.westlin.studiobooking.domain.model.TrainingSessionId

sealed class DomainException(message: String) : RuntimeException(message)

class TrainingSessionNotFoundException(id: TrainingSessionId) :
    DomainException("Training session with id '$id' was not found.")

class TrainingSessionFullException(val sessionId: TrainingSessionId) :
    DomainException("Training session '$sessionId' is full")

class TrainingSessionAlreadyCancelledException(val sessionId: TrainingSessionId) :
    DomainException("Training session '$sessionId' is already cancelled")

class MemberAlreadyBookedException(
    val sessionId: TrainingSessionId,
    val memberId: MemberId
) : DomainException("Member '$memberId' is already booked for session '$sessionId'")

class MemberNotFoundException(
    val memberId: MemberId,
) : DomainException("Member '$memberId' not found")

class MemberCannotBookException(
    val memberId: MemberId,
    val memberStatus: MemberStatus
) : DomainException("Member '$memberId' with status $memberStatus can't book session")

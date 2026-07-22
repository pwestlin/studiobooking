package nu.westlin.studiobooking.application

import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId

interface NotificationService {
    fun notifyMemberPromotedFromWaitlist(memberId: MemberId, sessionId: TrainingSessionId)
    fun notifyMemberAddedToWaitlist(memberId: MemberId, sessionId: TrainingSessionId, position: Int)
}
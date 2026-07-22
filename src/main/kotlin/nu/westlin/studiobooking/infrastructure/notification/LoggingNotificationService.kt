package nu.westlin.studiobooking.infrastructure.notification

import nu.westlin.studiobooking.application.NotificationService
import nu.westlin.studiobooking.domain.model.MemberId
import nu.westlin.studiobooking.domain.model.TrainingSessionId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LoggingNotificationService : NotificationService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun notifyMemberPromotedFromWaitlist(memberId: MemberId, sessionId: TrainingSessionId) {
        log.info(
            "NOTIS [E-POST/PUSH]: Medlem {} har flyttats upp från väntelistan och fått en ordinarie plats på pass {}!",
            memberId.value,
            sessionId.value
        )
    }

    override fun notifyMemberAddedToWaitlist(memberId: MemberId, sessionId: TrainingSessionId, position: Int) {
        log.info(
            "NOTIS [E-POST/PUSH]: Medlem {} placerades på väntelista på plats {} för pass {}.",
            memberId.value,
            position,
            sessionId.value
        )
    }
}
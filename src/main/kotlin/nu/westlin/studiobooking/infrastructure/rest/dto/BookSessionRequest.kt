package nu.westlin.studiobooking.infrastructure.rest.dto

import java.util.UUID

data class BookSessionRequest(
    val memberId: UUID
)
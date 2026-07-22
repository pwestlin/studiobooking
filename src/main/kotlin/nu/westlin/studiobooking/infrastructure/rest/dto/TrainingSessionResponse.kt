package nu.westlin.studiobooking.infrastructure.rest.dto

data class BookSessionResponse(
    val status: String,
    val waitlistPosition: Int? = null
)
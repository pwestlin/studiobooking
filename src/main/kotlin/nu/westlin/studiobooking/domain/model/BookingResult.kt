package nu.westlin.studiobooking.domain.model

sealed interface BookingResult {
    data object BookedSuccessfully : BookingResult
    data class AddedToWaitlist(val position: Int) : BookingResult

    sealed interface Failure : BookingResult {
        data object AlreadyBooked : Failure
        data object AlreadyInWaitlist : Failure
    }
}
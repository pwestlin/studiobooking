package nu.westlin.studiobooking.domain.model

sealed interface CancellationResult {
    data object CancelledSuccessfully : CancellationResult
    data class CancelledAndPromotedFromWaitlist(
        val promotedMemberId: MemberId
    ) : CancellationResult

    sealed interface Failure : CancellationResult {
        data object NotBooked : Failure
    }
}
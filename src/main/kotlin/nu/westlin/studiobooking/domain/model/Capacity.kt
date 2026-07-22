package nu.westlin.studiobooking.domain.model

@JvmInline
value class Capacity(val value: Int) {
    init {
        require(value > 0) { "Capacity must be greater than zero" }
    }
}
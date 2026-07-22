package nu.westlin.studiobooking.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CapacityTest {

    @Test
    fun `should create valid capacity`() {
        val capacity = Capacity(10)
        assertEquals(10, capacity.value)
    }

    @Test
    fun `should throw IllegalArgumentException when capacity is zero`() {
        val exception = assertThrows<IllegalArgumentException> {
            Capacity(0)
        }
        assertEquals("Capacity must be greater than zero", exception.message)
    }

    @Test
    fun `should throw IllegalArgumentException when capacity is negative`() {
        val exception = assertThrows<IllegalArgumentException> {
            Capacity(-5)
        }
        assertEquals("Capacity must be greater than zero", exception.message)
    }
}
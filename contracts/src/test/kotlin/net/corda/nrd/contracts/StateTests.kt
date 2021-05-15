package net.corda.nrd.contracts

import net.corda.nrd.states.CustomTokenState
import groovy.util.GroovyTestCase.assertEquals
import net.corda.testing.node.MockServices
import org.junit.Test

class StateTests {
    private val ledgerServices = MockServices()

    @Test
    fun hasMessageFieldOfCorrectType() {
        // Does the message field exist?
        CustomTokenState::class.java.getDeclaredField("message")
        // Is the message field of the correct type?
        assertEquals(CustomTokenState::class.java.getDeclaredField("message").type, String::class.java)
    }
}

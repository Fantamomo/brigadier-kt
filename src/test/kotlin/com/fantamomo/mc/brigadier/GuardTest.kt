package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GuardTest {

    data class Player(val name: String, val isAdmin: Boolean = false)

    @Test
    fun `guard allows execution`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                guard {
                    continueCommand()
                }
                execute { 1 }
            }
        )

        assertEquals(1, dispatcher.execute("test", Player("A")))
    }

    @Test
    fun `guard blocks execution`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                guard {
                    abort()
                }
                execute { 999 }
            }
        )

        val result = dispatcher.execute("test", Player("A"))
        assertEquals(0, result) // NO_SUCCESS
    }

    @Test
    fun `runOnSameNode false skips root execution`() {
        val dispatcher = CommandDispatcher<Player>()

        var guardExecuted = false

        dispatcher.register(
            command<Player>("test") {
                guard(false) {
                    guardExecuted = true
                    continueCommand()
                }

                execute { 1 }

                literal("child") {
                    execute { 2 }
                }
            }
        )

        dispatcher.execute("test", Player("A"))
        assertFalse(guardExecuted)

        dispatcher.execute("test child", Player("A"))
        assertTrue(guardExecuted)
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CommandExecutionTest {

    data class Player(val name: String)

    @Test
    fun `basic command executes`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("hello") {
                execute { 1 }
            }
        )

        val result = dispatcher.execute("hello", Player("Test"))
        assertEquals(1, result)
    }

    @Test
    fun `unknown command throws`() {
        val dispatcher = CommandDispatcher<Player>()

        assertFails {
            dispatcher.execute("unknown", Player("Test"))
        }
    }

    @Test
    fun `nested command executes`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("root") {
                literal("child") {
                    execute { 5 }
                }
            }
        )

        val result = dispatcher.execute("root child", Player("Test"))
        assertEquals(5, result)
    }
}
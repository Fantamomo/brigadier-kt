package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ContextMutationTest {

    data class Player(val name: String)

    @Test
    fun `setArgument overrides parsed value`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("name", StringArgumentType.word()) {

                    guard {
                        setArgument("name", "OVERRIDE")
                        continueCommand()
                    }

                    execute {
                        assertEquals("OVERRIDE", arg<String>("name"))
                        1
                    }
                }
            }
        )

        dispatcher.execute("test original", Player("A"))
    }

    @Test
    fun `removeArgument makes access fail`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("name", StringArgumentType.word()) {

                    guard {
                        removeArgument("name")
                        continueCommand()
                    }

                    execute {
                        assertFails {
                            arg<String>("name")
                        }
                        1
                    }
                }
            }
        )

        dispatcher.execute("test hello", Player("A"))
    }

    @Test
    fun `resetArgument restores original value`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("name", StringArgumentType.word()) {

                    guard {
                        setArgument("name", "override")
                        resetArgument("name")
                        continueCommand()
                    }

                    execute {
                        assertEquals("hello", arg<String>("name"))
                        1
                    }
                }
            }
        )

        dispatcher.execute("test hello", Player("A"))
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse

class EdgeCaseTest {

    data class Player(val name: String)

    @Test
    fun `invalid argument type fails`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("num", IntegerArgumentType.integer()) {
                    execute { 1 }
                }
            }
        )

        assertFails {
            dispatcher.execute("test abc", Player("Test"))
        }
    }

    @Test
    fun `guard abort prevents execution`() {
        val dispatcher = CommandDispatcher<Player>()

        var executed = false

        dispatcher.register(
            command<Player>("test") {
                guard { abort() }

                execute {
                    executed = true
                    1
                }
            }
        )

        dispatcher.execute("test", Player("Test"))

        assertFalse(executed)
    }

    @Test
    fun `empty fork cancels execution`() {
        val dispatcher = CommandDispatcher<Player>()

        var executed = false

        dispatcher.register(
            command<Player>("root") {
                execute {
                    executed = true
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("fork") {
                fork(dispatcher.root) {
                    emptyList()
                }
            }
        )

        dispatcher.execute("fork root", Player("Test"))

        assertFalse(executed)
    }
}
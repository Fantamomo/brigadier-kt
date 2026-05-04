package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import org.junit.jupiter.api.Test
import kotlin.test.assertFails
import kotlin.test.assertNull

class RoutingStorageExceptionTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `exception removes corrupted context`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertNull(context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, "value")
                    error("boom")
                }
            }
        )

        assertFails {
            dispatcher.execute("route base", Player("A"))
        }

        // next execution must be clean
        dispatcher.execute("base", Player("A"))
    }

    @Test
    fun `partial routing does not corrupt next execution`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertNull(context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, "value")
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()
                }
            }
        )

        assertFails {
            dispatcher.execute("route base", Player("A"))
        }

        dispatcher.execute("base", Player("A"))
    }
}
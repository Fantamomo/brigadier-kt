package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RoutingTest {

    data class Player(val name: String)

    private val KEY = createStaticRoutingKey<Player, String?>("test", null)

    @Test
    fun `routing overrides context`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    val value = context(KEY)
                    assertEquals("abc", value)
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, "abc")
                }
            }
        )

        dispatcher.execute("route base", Player("Test"))
    }

    @Test
    fun `routing fallback works`() {
        val dispatcher = CommandDispatcher<Player>()

        val key = createDynamicRoutingKey<Player, String>("fallback") {
            "default"
        }

        dispatcher.register(
            command<Player>("test") {
                execute {
                    assertEquals("default", context(key))
                    1
                }
            }
        )

        dispatcher.execute("test", Player("Test"))
    }
}
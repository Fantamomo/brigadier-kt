package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutingStorageLifecycleTest {

    data class Player(val name: String)

    private val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `store and load works within same execution`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertEquals("value", context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, "value")
                }
            }
        )

        dispatcher.execute("route base", Player("A"))
    }

    @Test
    fun `storage resets after execution`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertNull(context(KEY))
                    1
                }
            }
        )

        dispatcher.execute("base", Player("A"))
    }
}
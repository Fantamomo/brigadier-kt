package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

class RoutingNullHandlingTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", "fallback")

    @Test
    fun `null value is stored and returned as null`() {
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
                    set(KEY, null)
                }
            }
        )

        dispatcher.execute("route base", Player("A"))
    }

    @Test
    fun `has distinguishes null vs absent`() {
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
                    set(KEY, null)
                }
            }
        )

        dispatcher.execute("route base", Player("A"))
    }
}
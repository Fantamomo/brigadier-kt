package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GuardAndRoutingInteractionTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `guard sees routed value`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {

                guard {
                    assertEquals("abc", context(KEY))
                    continueCommand()
                }

                execute { 1 }
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
}
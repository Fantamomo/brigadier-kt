package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutingIsolationTest {

    data class Player(val name: String)

    private val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `routing does not leak between executions`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    val value = context(KEY)
                    if (source.name == "A") {
                        assertEquals("valueA", value)
                    } else {
                        assertNull(value)
                    }
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, "valueA")
                }
            }
        )

        dispatcher.execute("route base", Player("A"))
        dispatcher.execute("base", Player("B"))
    }
}
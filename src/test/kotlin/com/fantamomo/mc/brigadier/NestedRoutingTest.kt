package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NestedRoutingTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String>("key", "default")

    @Test
    fun `nested routing overrides previous value`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertEquals("second", context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("mid") {
                routing(dispatcher.root) {
                    set(KEY, "second")
                }
            }
        )

        dispatcher.register(
            command<Player>("start") {
                routing(dispatcher.root) {
                    set(KEY, "first")
                }
            }
        )

        dispatcher.execute("start mid base", Player("Test"))
    }
}
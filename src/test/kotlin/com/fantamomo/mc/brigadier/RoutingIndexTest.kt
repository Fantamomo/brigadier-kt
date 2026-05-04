package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RoutingIndexTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `multiple routing layers keep correct values`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    assertEquals("layer2", context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("mid") {
                routing(dispatcher.root) {
                    set(KEY, "layer2")
                }
            }
        )

        dispatcher.register(
            command<Player>("start") {
                routing(dispatcher.root) {
                    set(KEY, "layer1")
                }
            }
        )

        dispatcher.execute("start mid base", Player("A"))
    }
}
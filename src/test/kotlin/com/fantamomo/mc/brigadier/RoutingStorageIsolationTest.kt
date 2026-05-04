package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutingStorageIsolationTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `different command input resets storage`() {
        val dispatcher = CommandDispatcher<Player>()

        var redirect = true

        dispatcher.register(
            command<Player>("base") {
                execute {
                    if (redirect) {
                        assertEquals("value", context(KEY))
                    } else {
                        assertNull(context(KEY))
                    }
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
        redirect = false
        dispatcher.execute("base", Player("A")) // must NOT reuse
    }

    @Test
    fun `same input but different execution does not leak`() {
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
        dispatcher.execute("base", Player("A"))
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RedirectForkTest {

    data class Player(val name: String)

    @Test
    fun `redirect changes source`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("target") {
                execute {
                    assertTrue(source.name.contains("redirected"))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("start") {
                redirect(dispatcher.root) {
                    Player(it.source.name + " redirected")
                }
            }
        )

        dispatcher.execute("start target", Player("A"))
    }

    @Test
    fun `fork executes multiple times`() {
        val dispatcher = CommandDispatcher<Player>()

        var counter = 0

        dispatcher.register(
            command<Player>("root") {
                execute {
                    counter++
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("fork") {
                fork(dispatcher.root) {
                    List(5) { Player("P$it") }
                }
            }
        )

        dispatcher.execute("fork root", Player("X"))

        assertEquals(5, counter)
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.test.assertEquals

class RoutingThreadLocalTest {

    data class Player(val name: String)

    val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @OptIn(ExperimentalAtomicApi::class)
    @Test
    fun `thread local storage is isolated`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("base") {
                execute {
                    val name = source.name
                    assertEquals(name, context(KEY))
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, context.source.name)
                }
            }
        )

        val successes = AtomicInt(0)

        val threads = List(5) { i ->
            Thread {
                dispatcher.execute("route base", Player("P$i"))
                successes.incrementAndFetch()
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(5, successes.load())
    }
}
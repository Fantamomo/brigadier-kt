package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContextBuilder
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RoutingContextSafetyTest {

    data class Player(val name: String)

    private val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `cannot set after close`() {
        val dispatcher = CommandDispatcher<Player>()
        val ctx = CommandContextBuilder<Player>(null, Player("A"), dispatcher.root, 0).build("test")
        val routing = KtCommandRoutingContext(ctx)

        routing.close()

        assertFailsWith<IllegalStateException> {
            routing.set(KEY, "value")
        }
    }

    @Test
    fun `cannot access from another thread`() {
        val dispatcher = CommandDispatcher<Player>()
        val ctx = CommandContextBuilder<Player>(null, Player("A"), dispatcher.root, 0).build("test")
        val routing = KtCommandRoutingContext(ctx)

        val thread = Thread {
            assertFailsWith<IllegalStateException> {
                routing.set(KEY, "value")
            }
        }

        thread.start()
        thread.join()
    }
}
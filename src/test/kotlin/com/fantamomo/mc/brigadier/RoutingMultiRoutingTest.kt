package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RoutingMultiRoutingTest {

    data class Player(val name: String)

    private val KEY = KtRoutingKey.static<Player, String?>("key", null)

    @Test
    fun `multiple routing layers`() {
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
                    set(KEY, getOrNull(KEY)?.let { "$it/" }.orEmpty() + "route")
                }
            }
        )

        dispatcher.execute("route route base", Player("route/route"))
        dispatcher.execute("route route route route route route base", Player("route/route/route/route/route/route"))
    }

    @Test
    fun `multiple routing layers with fork`() {
        val dispatcher = CommandDispatcher<Player>()

        var count = 0

        dispatcher.register(
            command<Player>("base") {
                execute {
                    val name = source.name
                    val actual = context(KEY)
                    assertNotNull(actual)
                    val (first, second) = name.split(" ", limit = 2)
                    assertEquals(actual, first)
                    assertEquals("(forked $count)", second)
                    count++
                    1
                }
            }
        )

        dispatcher.register(
            command<Player>("route") {
                routing(dispatcher.root) {
                    set(KEY, getOrNull(KEY)?.let { "$it/" }.orEmpty() + "route")
                }
            }
        )

        dispatcher.register(
            command<Player>("multi") {
                fork(dispatcher.root) { context ->
                    List(5) { Player(context.source.name + " (forked $it)") }
                }
            }
        )

        dispatcher.execute("multi route route base", Player("route/route"))
        assertEquals(5, count)
    }

}
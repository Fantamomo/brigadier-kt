package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DeepTreeTest {

    data class Player(val name: String)

    @Test
    fun `deep nested command executes`() {
        val dispatcher = CommandDispatcher<Player>()

        val depth = 50

        var builder = command<Player>("root") {}
        repeat(depth) { i ->
            builder = command<Player>("root") {
                fun buildNestedCommands(current: KtLiteralCommandBuilder<Player>, level: Int) {
                    current.literal("n$level") {
                        if (level < i - 1) {
                            buildNestedCommands(this, level + 1)
                        } else {
                            execute { 1 }
                        }
                    }
                }
                buildNestedCommands(this, 0)
            }
        }

        dispatcher.register(builder)

        val command = buildString {
            append("root")
            repeat(depth - 1) { append(" n$it") }
        }

        val result = dispatcher.execute(command, Player("Test"))
        assertEquals(1, result)
    }
}
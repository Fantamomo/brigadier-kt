package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RegressionTest {

    data class Player(val name: String)

    @Test
    fun `multiple argRef do not conflict`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("a", IntegerArgumentType.integer()) {
                    val aRef = argRef()

                    argument("b", IntegerArgumentType.integer()) {
                        val bRef = argRef()

                        execute {
                            assertEquals(1, aRef.get())
                            assertEquals(2, bRef.get())
                            1
                        }
                    }
                }
            }
        )

        dispatcher.execute("test 1 2", Player("Test"))
    }

    @Test
    fun `guard mutation does not affect sibling branch`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("test") {
                argument("x", IntegerArgumentType.integer()) {

                    guard {
                        setArgument("x", 999)
                        continueCommand()
                    }

                    literal("a") {
                        execute {
                            assertEquals(999, arg<Int>("x"))
                            1
                        }
                    }

                    literal("b") {
                        execute {
                            assertEquals(999, arg<Int>("x"))
                            1
                        }
                    }
                }
            }
        )

        dispatcher.execute("test 1 a", Player("Test"))
        dispatcher.execute("test 1 b", Player("Test"))
    }
}
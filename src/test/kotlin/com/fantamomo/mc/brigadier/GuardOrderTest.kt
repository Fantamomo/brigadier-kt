package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GuardOrderTest {

    data class Player(val name: String)

    @Test
    fun `guards execute in root to leaf order`() {
        val dispatcher = CommandDispatcher<Player>()

        val order = mutableListOf<String>()

        dispatcher.register(
            command<Player>("root") {

                guard {
                    order += "root"
                    continueCommand()
                }

                literal("child") {

                    guard {
                        order += "child"
                        continueCommand()
                    }

                    execute { 1 }
                }
            }
        )

        dispatcher.execute("root child", Player("Test"))

        assertEquals(listOf("root", "child"), order)
    }

    @Test
    fun `guard abort stops further guards`() {
        val dispatcher = CommandDispatcher<Player>()

        val order = mutableListOf<String>()

        dispatcher.register(
            command<Player>("root") {

                guard {
                    order += "root"
                    abort()
                }

                literal("child") {

                    guard {
                        order += "child"
                        continueCommand()
                    }

                    execute { 1 }
                }
            }
        )

        dispatcher.execute("root child", Player("Test"))

        assertEquals(listOf("root"), order)
    }
}
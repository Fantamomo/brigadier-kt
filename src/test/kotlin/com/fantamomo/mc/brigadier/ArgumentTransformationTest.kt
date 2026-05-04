package com.fantamomo.mc.brigadier

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ArgumentTransformationTest {

    data class Player(val name: String)
    data class Level(val value: Int)

    @Test
    fun `argument is transformed via guard`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("level") {
                argument("value", IntegerArgumentType.integer()) {

                    val raw = argRef()
                    val transformed = createArgRef("value", Level::class)

                    guard {
                        transformed.set(Level(raw.get()))
                        continueCommand()
                    }

                    execute {
                        val lvl = transformed.get()
                        assertEquals(42, lvl.value)
                        1
                    }
                }
            }
        )

        dispatcher.execute("level 42", Player("Test"))
    }

    @Test
    fun `missing transformation crashes`() {
        val dispatcher = CommandDispatcher<Player>()

        dispatcher.register(
            command<Player>("level") {
                argument("value", IntegerArgumentType.integer()) {

                    val transformed = createArgRef("value", Level::class)

                    execute {
                        assertFails {
                            transformed.get()
                        }
                        1
                    }
                }
            }
        )

        dispatcher.execute("level 5", Player("Test"))
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode

/**
 * A command builder specifically designed for creating literal commands.
 *
 * The `KtLiteralCommandBuilder` serves as a specialized implementation of
 * `KtCommandBuilder` for defining literal-based commands. Literal commands
 * are commands that must match a specific word or phrase to be executed.
 * This class uses the Brigadier framework's `LiteralArgumentBuilder` to
 * construct such commands.
 *
 * @param S The type of the command source (e.g., player, console).
 * @param literal The exact text or phrase that the command should match.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
class KtLiteralCommandBuilder<S>(literal: String) : KtCommandBuilder<S, LiteralArgumentBuilder<S>>() {
    @PublishedApi
    override val builder: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(literal)

    override fun build(): LiteralCommandNode<S> = builder.build()
}
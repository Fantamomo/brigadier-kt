package com.fantamomo.mc.brigadier

import com.mojang.brigadier.tree.CommandNode

/**
 * Creates a `CommandNode` for a literal command using the specified literal string and a configuration block.
 *
 * This method allows defining a literal command with a specific text that must be matched
 * in a command context. The behavior and structure of the command are configured
 * within the provided DSL block, allowing for a concise and readable definition.
 *
 * @param literal The exact text or phrase that the command should match.
 * @param block A DSL configuration block used to specify the command's behavior and structure.
 * @return The constructed `CommandNode` representing the configured literal command.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> command(literal: String, block: KtLiteralCommandBuilder<S>.() -> Unit): CommandNode<S> {
    val builder = KtLiteralCommandBuilder<S>(literal)
    builder.block()
    return builder.build()
}
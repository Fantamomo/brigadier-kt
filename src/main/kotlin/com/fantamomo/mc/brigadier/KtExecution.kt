package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext

/**
 * Defines the command execution behavior.
 *
 * Registers the provided block of code as the action to be executed
 * when the command is invoked. The block operates within the context
 * of the command source and returns an integer result.
 *
 * @param block A lambda representing the command's execution logic.
 *              It is a DSL block operating in the scope of
 *              `CommandContext<S>` and returns an integer value
 *              typically representing the command's result code.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.execute(block: @KtCommandDsl CommandContext<S>.() -> Int) {
    builder.executes(block)
}
package com.fantamomo.mc.brigadier

/**
 * Sets a condition that determines whether this command can be used by a specific command source.
 *
 * This method allows specifying a predicate that evaluates whether the command
 * is available for the current context. The predicate is defined as a block
 * operating on the command source type.
 *
 * @param block A lambda representing the condition for the command's availability.
 *              It operates in the scope of the command source and returns a
 *              Boolean indicating whether the command can be used.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.requires(block: @KtCommandDsl S.() -> Boolean) {
    builder.requires(block)
}
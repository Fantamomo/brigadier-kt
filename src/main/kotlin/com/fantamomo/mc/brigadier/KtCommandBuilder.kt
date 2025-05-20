package com.fantamomo.mc.brigadier

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode

/**
 * A base class for building command trees using a DSL-style approach.
 *
 * The `KtCommandBuilder` serves as a foundation for creating commands in
 * Minecraft-like systems using the Brigadier framework.
 * Subclasses like
 * `KtLiteralCommandBuilder` and `KtArgumentCommandBuilder` extend this
 * class to provide specific implementations for literal and argument
 * commands.
 * This class uses the `KtCommandDsl` annotation to enforce
 * contextual DSL rules.
 *
 * @param S The type of the command source (e.g., player, console).
 * @param B The specific type of the `ArgumentBuilder` being used (e.g.,
 *          `LiteralArgumentBuilder`, `RequiredArgumentBuilder`).
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@KtCommandDsl
abstract class KtCommandBuilder<S, B : ArgumentBuilder<S, B>> {
    /**
     * Represents the underlying `ArgumentBuilder` associated with a command builder instance.
     *
     * This property is used to define and configure the core structure of a command within the
     * Brigadier framework.
     * The specific type of `ArgumentBuilder` is determined by the subclasses
     * of `KtCommandBuilder`, such as `KtLiteralCommandBuilder` for literal commands or
     * `KtArgumentCommandBuilder` for argument-based commands.
     *
     * Subclasses should override this property to provide the appropriate `ArgumentBuilder`
     * implementation, enabling the DSL-style definitions for commands.
     */
    internal abstract val builder: B

    /**
     * Builds and returns the resulting `CommandNode` for the current command structure.
     *
     * This method finalizes the configuration of the command and converts it into
     * a `CommandNode` that can be used within the Brigadier command framework.
     *
     * @return the constructed `CommandNode` representing the command structure.
     */
    open fun build(): CommandNode<S> = builder.build()
}
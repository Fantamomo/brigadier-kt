package com.fantamomo.mc.brigadier

import com.fantamomo.mc.brigadier.internal.GuardList
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder

/**
 * A command builder for creating argument-based commands in a DSL-style approach.
 *
 * This class extends `KtCommandBuilder` to provide specific functionality for
 * handling commands with arguments using Brigadier's `RequiredArgumentBuilder`.
 *
 * The `KtArgumentCommandBuilder` is designed to facilitate the creation and configuration
 * of argument-based commands, allowing developers to define commands that accept
 * specific types of arguments. It uses DSL constructs to simplify and structure
 * the configuration process.
 *
 * @param S The type representing the command source (e.g., player, console).
 * @param T The type of the argument that this command expects.
 * @param name The name of the argument, used as its identifier in the command structure.
 * @param type The type of the argument, represented by Brigadier's `ArgumentType`.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
class KtArgumentCommandBuilder<S, T>(
    name: String,
    type: ArgumentType<T>,
    override val guards: GuardList<S>
) : KtCommandBuilder<S, RequiredArgumentBuilder<S, T>>() {
    override val builder: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(name, type)
}
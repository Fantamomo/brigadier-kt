package com.fantamomo.mc.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Adds a literal subcommand to the current command builder.
 *
 * This method allows defining a literal subcommand with a specific text that must be matched
 * in a command context. The structure and behavior of the subcommand can be configured
 * within the provided DSL block.
 *
 * @param literal The exact text or phrase that the subcommand should match.
 * @param block A DSL configuration block used to specify the structure and behavior
 *              of the literal subcommand.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@OptIn(ExperimentalContracts::class)
fun <S> KtCommandBuilder<S, *>.literal(literal: String, block: KtLiteralCommandBuilder<S>.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val literalBuilder = KtLiteralCommandBuilder<S>(literal)
    literalBuilder.block()
    builder.then(literalBuilder.build())
}

fun <S> KtCommandBuilder<S, *>.literalExecute(literal: String, block: @KtCommandDsl CommandContext<S>.() -> Int) {
    val builder = LiteralArgumentBuilder.literal<S>(literal)
    builder.executes(block)
    this.builder.then(builder.build())
}

/**
 * Adds an argument to the command being built.
 *
 * This method is used to define an argument-based command node. The argument is identified
 * by its name and type, and further customized through the provided DSL block. It integrates
 * the argument node into the command tree defined by the builder.
 *
 * @param name The name of the argument, used as its identifier within the command structure.
 * @param type The type of the argument, represented by an `ArgumentType` instance.
 * @param block A DSL configuration block used to define and customize the behavior
 *              of the argument-based command node.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@OptIn(ExperimentalContracts::class)
fun <S, T> KtCommandBuilder<S, *>.argument(name: String, type: ArgumentType<T>, block: KtArgumentCommandBuilder<S, T>.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val argumentBuilder = KtArgumentCommandBuilder<S, T>(name, type)
    argumentBuilder.block()
    builder.then(argumentBuilder.build())
}
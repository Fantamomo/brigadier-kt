package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext
import kotlin.reflect.KClass

/**
 * Represents a reference to a named argument with a specific type used in a command context.
 *
 * This class is primarily used to facilitate argument storage or retrieval operations
 * within commands built using the Kotlin DSL for Brigadier-based systems. It pairs
 * an argument name with its corresponding type, ensuring type safety when manipulating
 * arguments in the command context.
 *
 * @param S The type of the command source.
 * @param T The type of the value associated with the referenced argument.
 * @constructor Creates a reference to a named argument with the specified type.
 * @property name The name of the referenced argument.
 * @property type The Kotlin class type of the referenced argument's value.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
class KtArgumentRef<S, T>(
    internal val name: String,
    internal val type: KClass<T & Any>
)

/**
 * Retrieves the value of the argument referenced by this [KtArgumentRef] from the command context.
 *
 * This method leverages the context provided by the surrounding command execution to retrieve
 * the argument value identified by the name and type defined in the [KtArgumentRef]. It ensures
 * type safety during retrieval, throwing runtime exceptions if the argument does not exist or
 * the types do not match.
 *
 * @return The value of type `T` associated with the argument reference.
 * @throws IllegalArgumentException if the argument specified by this reference is not found or does not match the expected type.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
context(ctx: CommandContext<S>)
fun <S, T> KtArgumentRef<S, T>.get(): T = ctx.arg(type, name)

/**
 * Sets the value of the current argument reference in the command context.
 *
 * This method uses the command context to update or override the value
 * of the argument identified by the reference's name. The new value will be
 * returned by later calls to retrieve the argument within the command execution.
 *
 * @param value The new value to set for the argument. It must match the type of the argument reference.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
context(ctx: KtCommandContext<S>)
fun <S, T> KtArgumentRef<S, T>.set(value: T) = ctx.setArgument(name, value)

/**
 * Creates a reference to a named argument with a specific type within the context of the command.
 *
 * This method is used to create an instance of [KtArgumentRef], which represents a binding between
 * the argument's name and its type. The resulting reference can be used in various operations
 * within the command to access or manipulate the argument's value safely and efficiently.
 *
 * @param clazz The Kotlin class type of the argument that this reference is associated with.
 *              It ensures type safety for the argument in the command context.
 * @return A `KtArgumentRef` representing the reference to the named argument and its type.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
fun <S, T> KtArgumentCommandBuilder<S, T>.argRef(clazz: KClass<T & Any>): KtArgumentRef<S, T> =
    KtArgumentRef(builder.name, clazz)

/**
 * Creates a reference to a named argument with a specific type within the command context.
 *
 * This method is used to generate a `KtArgumentRef` for the argument that was previously
 * defined in this [KtArgumentCommandBuilder]. It leverages reified type parameters
 * to infer the type of the argument, ensuring type safety at compile time.
 *
 * @return A `KtArgumentRef` that represents the association between the name
 *         and type of the argument within the builder.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
inline fun <S, reified T : Any> KtArgumentCommandBuilder<S, T>.argRef(): KtArgumentRef<S, T> =
    argRef(T::class)

/**
 * Creates a reference to an argument within the command context.
 *
 * This function generates a [KtArgumentRef] instance that associates a specific argument
 * name with its corresponding type. The reference can be used to retrieve or manipulate
 * the argument value in a type-safe manner during command execution.
 *
 * @param name The name of the argument to be referenced.
 * @param clazz The Kotlin class type of the argument's value.
 * @return A `KtArgumentRef` object representing the named argument and its type.
 *
 * @author Fantamomo
 * @since 1.3-SNAPSHOT
 */
fun <S, T> KtArgumentCommandBuilder<S, *>.createArgRef(name: String, clazz: KClass<T & Any>): KtArgumentRef<S, T> =
    KtArgumentRef(name, clazz)
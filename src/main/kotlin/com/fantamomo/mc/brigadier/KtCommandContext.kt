package com.fantamomo.mc.brigadier

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import kotlin.reflect.KClass

/**
 * A predefined constant representing a successful execution result in the context of a command execution.
 *
 * This property uses the `SINGLE_SUCCESS` value from the Brigadier library.
 * It is typically used to
 * indicate successful execution or to act as a return value for commands that are complete without errors.
 *
 * In the context of a Minecraft-style command framework, this value corresponds to a standard success outcome
 * that commands can return to signify their successful execution within a `CommandContext`.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@Suppress("UnusedReceiverParameter")
val CommandContext<*>.SINGLE_SUCCESS: Int get() = Command.SINGLE_SUCCESS

/**
 * Retrieves an argument of the specified type and name from the command context.
 *
 * This method fetches an argument from the command context using the provided
 * class type and argument name. It ensures that the argument matches the expected
 * type before returning it.
 *
 * @param kclass The Kotlin class of the expected argument type.
 * @param name The name of the argument to retrieve.
 * @return The argument of type `T` corresponding to the specified name.
 * @throws IllegalArgumentException if the argument is not found or does not match the expected type.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <T : Any> CommandContext<*>.arg(kclass: KClass<T>, name: String): T =
    this.getArgument(name, kclass.javaPrimitiveType ?: kclass.java)

/**
 * Retrieves an optional argument of the specified type and name from the command context.
 *
 * This method attempts to fetch an argument from the command context identified
 * by the provided class type and argument name. If the argument is not found or
 * does not match the expected type, it returns null instead of throwing an exception.
 *
 * @param T The type of the argument being retrieved.
 * @param kclass The Kotlin class of the expected argument type.
 * @param name The name of the argument to retrieve.
 * @return The argument of type `T` corresponding to the specified name, or null if
 *         the argument is not found or does not match the expected type.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <T : Any> CommandContext<*>.optionalArg(kclass: KClass<T>, name: String): T? = try {
    arg(kclass, name)
} catch (e: IllegalArgumentException) {
    null
}

/**
 * Retrieves an argument of the specified type and name from the command context.
 *
 * This method fetches an argument of type `T` from the command context using the provided
 * argument name and the reified type parameter. It ensures the argument matches the expected
 * type before returning it.
 *
 * @param name The name of the argument to retrieve.
 * @return The argument of type `T` corresponding to the specified name.
 * @throws IllegalArgumentException if the argument is not found or does not match the expected type.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
inline fun <reified T : Any> CommandContext<*>.arg(name: String): T = arg(T::class, name)

@Suppress("unused")
inline fun <reified T : Any> CommandContext<*>.optionalArg(name: String): T? = optionalArg(T::class, name)
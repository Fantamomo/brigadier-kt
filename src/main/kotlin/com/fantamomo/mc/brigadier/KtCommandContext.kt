package com.fantamomo.mc.brigadier

import com.fantamomo.mc.brigadier.internal.Symbol
import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.ParsedArgument
import com.mojang.brigadier.context.ParsedCommandNode
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.tree.CommandNode
import kotlin.reflect.KClass

/**
 * An extended [CommandContext] that allows argument overriding and mutation.
 *
 * This class wraps Brigadier's original [CommandContext] and enables
 * Guards to:
 *
 * - Override parsed arguments
 * - Inject additional arguments
 * - Replace raw values with domain objects
 *
 * The overridden arguments take precedence over Brigadier's parsed arguments.
 *
 * @param S The command source type.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
class KtCommandContext<S>(
    source: S?,
    input: String?,
    arguments: Map<String, ParsedArgument<S, *>>,
    command: Command<S>?,
    rootNode: CommandNode<S>?,
    nodes: List<ParsedCommandNode<S>>?,
    range: StringRange?,
    child: CommandContext<S>?,
    modifier: RedirectModifier<S>?,
    forks: Boolean
) : CommandContext<S>(
    source, input, arguments,
    command, rootNode, nodes,
    range, child, modifier, forks
) {
    private val overriddenArgs: MutableMap<String, Any> = mutableMapOf()

    companion object {
        private val NULL = Symbol("NULL")
        private val REMOVED = Symbol("REMOVED")
        private val argumentField =
            CommandContext::class.java.getDeclaredField("arguments").apply { isAccessible = true }

        fun <S> of(other: CommandContext<S>): KtCommandContext<S> {
            if (other is KtCommandContext<S>) return other.clone()
            return of0(other)
        }

        private fun <S> of0(other: CommandContext<S>) = other.run {
            KtCommandContext<S>(
                source, input, arguments,
                command, rootNode, nodes,
                range, child,
                redirectModifier,
                isForked
            )
        }

        @Suppress("UNCHECKED_CAST")
        private val <S> CommandContext<S>.arguments: Map<String, ParsedArgument<S, *>>
            get() = argumentField.get(this) as Map<String, ParsedArgument<S, *>>
    }

    fun clone(): KtCommandContext<S> {
        val clone = of0(this)
        clone.overriddenArgs.putAll(overriddenArgs)
        return clone
    }

    @Suppress("UNCHECKED_CAST")
    override fun <V> getArgument(name: String, clazz: Class<V>): V {
        val overridden = overriddenArgs[name]
        return when (overridden) {
            NULL -> null as V
            REMOVED -> throw IllegalArgumentException("Argument '$name' has been removed from the context")
            else -> super.getArgument(name, clazz)
        }
    }

    /**
     * Overrides or injects an argument into this command context.
     *
     * The provided value will be returned by further calls to
     * [getArgument] and [arg].
     *
     * @param name The argument name.
     * @param value The value to associate with the argument.
     */
    fun setArgument(name: String, value: Any?) {
        overriddenArgs[name] = value ?: NULL
    }

    /**
     * Removes the argument with the specified name from the context.
     *
     * This method marks the argument identified by the given name
     * as removed within the context.
     *
     * Future calls to [getArgument] or [arg] will throw an exception.
     *
     * @param name The name of the argument to remove.
     */
    fun removeArgument(name: String) {
        overriddenArgs[name] = REMOVED
    }

    /**
     * Resets the specified argument by removing it from the context.
     *
     * This method clears any value associated with the given argument name in the
     * overridden arguments of this command context. Future calls to fetch the argument
     * will refer to its original value or throw an exception if it's not defined.
     *
     * @param name The name of the argument to reset.
     */
    fun resetArgument(name: String) {
        overriddenArgs.remove(name)
    }
}

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
 * Represents a constant result code indicating that a command execution has failed or was unsuccessful.
 *
 * Typically, a return value of `NO_SUCCESS` might be used to notify the system
 * or the framework that the command's intended effect was not achieved.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
val CommandContext<*>.NO_SUCCESS: Int get() = 0

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
 *
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
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <T : Any> CommandContext<*>.optionalArg(kclass: KClass<T>, name: String): T? = try {
    arg(kclass, name)
} catch (_: IllegalArgumentException) {
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
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
inline fun <reified T : Any> CommandContext<*>.arg(name: String): T = arg(T::class, name)

/**
 * Retrieves an optional argument of the specified type and name from the command context.
 *
 * This method simplifies the process of fetching an optional argument by implicitly
 * inferring the type from the reified type parameter. If the argument is not present
 * or does not match the specified type, it returns null.
 *
 * @param T The type of the argument being retrieved.
 * @param name The name of the argument to retrieve from the command context.
 * @return The argument of type `T` corresponding to the specified name, or null if
 *         the argument is not found or does not match the expected type.
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@Suppress("unused")
inline fun <reified T : Any> CommandContext<*>.optionalArg(name: String): T? = optionalArg(T::class, name)
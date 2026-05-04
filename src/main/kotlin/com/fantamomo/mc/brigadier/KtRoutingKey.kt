package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext

/**
 * A functional interface for providing a value of type [T] based on a [CommandContext].
 *
 * Implementations of this interface define a single method, [provide], which extracts
 * or computes a value of type [T] using the given command execution context.
 *
 * This interface facilitates resolution and extraction of contextual or routing-specific
 * values during command execution.
 *
 * @param S The sender type associated with the command context.
 * @param T The type of value to be provided based on the context.
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 * @see KtRoutingKey
 */
fun interface KtRoutingValueProvider<S, T> {
    /**
     * Provides a value of type [T] based on the given command context [context].
     *
     * @param context The command context used to extract or compute the value.
     * @return The computed or retrieved value of type [T] based on the provided context.
     */
    fun provide(context: CommandContext<S>): T
}

/**
 * Represents a typed key used to attach and retrieve routing-scoped context values.
 *
 * A [KtRoutingKey] defines how a value is resolved during command execution:
 *
 * - If a value was provided via [routing], that value is used
 * - Otherwise, [provide] is invoked
 *
 * This allows commands to access contextual data without modifying the source ([S]).
 *
 * @param name A unique identifier for this key.
 * @param cacheValue Whether to cache the resolved fallback value per execution.
 *                   When `false`, the value is never cached. When `true`, caching behavior
 *                   is implementation-dependent and may or may not occur.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 * @see CommandContext.context
 * @see KtCommandRoutingContext.set
 */
abstract class KtRoutingKey<S, T>(
    val name: String,
    val cacheValue: Boolean = true
) : KtRoutingValueProvider<S, T> {
    /**
     * Computes the fallback value when no routing value is present.
     *
     * This is typically used to derive a default value from the current [CommandContext],
     * such as resolving the player's current game.
     *
     * @param context The current command context.
     * @return The fallback value.
     */
    abstract override fun provide(context: CommandContext<S>): T

    companion object {
        /**
         * Creates a dynamic routing key with a fallback provider.
         *
         * @param name The key name.
         * @param cacheValue Whether to cache the fallback result.
         * @param fallbackProvider Function used to compute the fallback value.
         */
        fun <S, T> dynamic(
            name: String,
            cacheValue: Boolean = true,
            fallbackProvider: KtRoutingValueProvider<S, T>
        ): KtRoutingKey<S, T> = DynamicRoutingKey(name, cacheValue, fallbackProvider)

        /**
         * Creates a routing key with a constant fallback value.
         *
         * @param name The key name.
         * @param value The constant value returned as fallback.
         */
        fun <S, T> static(
            name: String,
            value: T
        ): KtRoutingKey<S, T> = StaticDynamicKey(name, value)
    }
}


/**
 * A dynamic implementation of [KtRoutingKey] that provides a fallback value through a lambda function.
 *
 * @param S The source type associated with the command context.
 * @param T The type of the value stored and retrieved using this routing key.
 * @param name A unique identifier for this key.
 * @param cacheValue Whether to cache the resolved fallback value during execution.
 *                   When `false`, the value is recomputed every time it is accessed.
 * @param fallbackProvider A lambda function that computes the fallback value given a [CommandContext].
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
private class DynamicRoutingKey<S, T>(
    name: String,
    cacheValue: Boolean,
    val fallbackProvider: KtRoutingValueProvider<S, T>
) : KtRoutingKey<S, T>(name, cacheValue), KtRoutingValueProvider<S, T> by fallbackProvider

/**
 * A specialized implementation of [KtRoutingKey] that provides a constant fallback value.
 *
 * @param S The source type from which routing information can be derived.
 * @param T The type of the value associated with this key.
 * @param name A unique identifier for this key.
 * @param value The constant fallback value to be returned in the absence of a routing value.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
private class StaticDynamicKey<S, T>(name: String, val value: T) : KtRoutingKey<S, T>(name, true) {
    override fun provide(context: CommandContext<S>): T = value
}

/**
 * Creates a dynamic routing key with the specified name, cache behavior, and fallback provider.
 *
 * @param name The unique name for the routing key.
 * @param cacheValue Determines whether the resolved fallback value should be cached during execution.
 *                   Defaults to `true`. Caching behavior is implementation-dependent.
 * @param fallbackProvider A function that computes the fallback value when no routing value is present.
 * @return A new instance of [KtRoutingKey] configured with the specified parameters.
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 * @see CommandContext.context
 * @see KtCommandRoutingContext.set
 */
fun <S, T> createDynamicRoutingKey(
    name: String,
    cacheValue: Boolean = true,
    fallbackProvider: (CommandContext<S>) -> T
): KtRoutingKey<S, T> = DynamicRoutingKey(name, cacheValue, fallbackProvider)

/**
 * Creates a static routing key with a given name and constant fallback value.
 *
 * @param name The unique name of the routing key.
 * @param value The constant fallback value to use if no value is explicitly set in the routing context.
 * @return A [KtRoutingKey] instance with the specified name and fallback value.
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 * @see CommandContext.context
 * @see KtCommandRoutingContext.set
 */
fun <S, T> createStaticRoutingKey(name: String, value: T): KtRoutingKey<S, T> = StaticDynamicKey(name, value)
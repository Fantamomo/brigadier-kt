package com.fantamomo.mc.brigadier

import com.fantamomo.mc.brigadier.internal.Symbol
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.tree.CommandNode

/**
 * Internal storage for routing values during command execution.
 *
 * This class manages per-execution routing state using a thread-local storage.
 * It ensures that routing values are:
 *
 * - scoped to a single command execution
 * - isolated per redirect chain
 * - correctly propagated into [KtCommandContext]
 *
 * Not intended for public use.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
internal object KtCommandRoutingStorage {
    val NULL = Symbol("NULL")

    private class RoutingStorage {
        var data: MutableList<MutableMap<KtRoutingKey<*, *>, Any?>> = mutableListOf()
        var executed: Int = 0
        var routed: Int = 0
        var currentIndex: Int = 0
            set(value) {
                field = value
                if (value > maxIndexed) {
                    maxIndexed = value
                }
            }
        var lastCommandPosition: StringRange = StringRange.at(0)
        var executionHash: Int? = null

        var maxIndexed: Int = 0

        fun canBeRemoved() = executed > maxIndexed
    }

    private val storage: ThreadLocal<RoutingStorage> = ThreadLocal()

    private fun getHashCode(ctx: CommandContext<*>) = ctx.input.hashCode()

    fun getForExecution(ctx: CommandContext<*>): MutableMap<KtRoutingKey<*, *>, Any?>? {
        val data = storage.get() ?: return null
        if (data.canBeRemoved()) {
            storage.remove()
            return null
        }
        if (data.executionHash == null) data.executionHash = getHashCode(ctx)
        else if (data.executionHash != getHashCode(ctx)) {
            storage.remove()
            return null
        }
        data.executed++
        return data.data.getOrNull(data.executed - 1)?.toMutableMap()
    }

    fun <T> store(context: KtCommandRoutingContext<*>, key: KtRoutingKey<*, T>, value: T) {
        var data = storage.get()
        if (data == null || data.executed > 0) { // should never happen due to init, but just in case
            data = RoutingStorage()
            storage.set(data)
        }
        val contextData = if (data.data.size == context.index) {
            val newData = mutableMapOf<KtRoutingKey<*, *>, Any?>()
            data.data.add(newData)
            newData
        } else {
            data.data[context.index]
        }
        contextData[key] = value ?: NULL
    }

    fun init(context: KtCommandRoutingContext<*>): Int {
        val value = storage.get()
        val range = context.context.range
        if (value != null) {
            if (value.executionHash == getHashCode(context.context)) {
                if (value.executed <= 0) {
                    if (value.lastCommandPosition != range) {
                        value.lastCommandPosition = range
                        value.currentIndex = 0
                    } else {
                        value.currentIndex++
                    }
                    return value.currentIndex
                }
            }
        }
        val routingStorage = RoutingStorage()
        routingStorage.routed++
        routingStorage.lastCommandPosition = range
        routingStorage.executionHash = getHashCode(context.context)
        storage.set(routingStorage)
        return routingStorage.currentIndex
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> load(context: KtCommandRoutingContext<*>, key: KtRoutingKey<*, T>): T? {
        val value = storage.get()?.data?.getOrNull(context.index)?.get(key)
        return (if (value === NULL) null else value) as? T
    }

    fun isPresent(context: KtCommandRoutingContext<*>, key: KtRoutingKey<*, *>): Boolean =
        storage.get()?.data?.getOrNull(context.index)?.contains(key) == true

    fun onException(context: KtCommandRoutingContext<*>) {
        val value = storage.get() ?: return
        value.data.removeAt(context.index)
        value.currentIndex--
        value.maxIndexed--
    }
}

/**
 * Represents a mutable routing context used during a routing redirect.
 *
 * This context allows attaching values to [KtRoutingKey]s which will later
 * be available during command execution via [CommandContext.context].
 *
 * Instances of this class are short-lived and only valid within the routing block and the same thread.
 *
 * @param context The underlying Brigadier command context.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
class KtCommandRoutingContext<S> internal constructor(val context: CommandContext<S>) {
    private val ownerThread = Thread.currentThread()
    private var closed = false

    internal val index = KtCommandRoutingStorage.init(this)

    /**
     * Sets a routing value for the given [key].
     *
     * This overrides the fallback value for the remainder of the execution chain.
     *
     * @throws IllegalStateException if the context is closed or accessed from another thread.
     */
    fun <T> set(key: KtRoutingKey<S, T>, value: T) {
        if (closed) throw IllegalStateException("Routing context has been closed")
        if (ownerThread !== Thread.currentThread()) throw IllegalStateException("Routing context can only be set on the owner thread")
        KtCommandRoutingStorage.store(this, key, value)
    }

    /**
     * Retrieves a previously set routing value.
     *
     * @throws IllegalStateException if no value is present.
     */
    fun <T> get(key: KtRoutingKey<S, T>): T =
        KtCommandRoutingStorage.load(this, key)
            ?: throw IllegalStateException("Routing context has no value for key '$key'")

    /**
     * Retrieves a routing value if present, otherwise returns null.
     */
    fun <T> getOrNull(key: KtRoutingKey<S, T>): T? = KtCommandRoutingStorage.load(this, key)

    /**
     * Checks whether a value for the given [key] exists in this routing context.
     */
    fun has(key: KtRoutingKey<S, *>): Boolean = KtCommandRoutingStorage.isPresent(this, key)

    internal fun close() {
        closed = true
    }

    internal fun onException() {
        KtCommandRoutingStorage.onException(this)
    }
}

/**
 * Redirects execution to [target] while attaching routing context values.
 *
 * This behaves like a normal Brigadier redirect, but additionally allows
 * injecting values into the execution context via [KtRoutingKey].
 *
 * The provided [block] is executed during the redirect phase and can call [KtCommandRoutingContext.set]
 * to override values for the remainder of the execution chain.
 *
 * Example:
 * ```kotlin
 * routing(baseNode) {
 *     set(GAME_KEY, context.arg("code"))
 * }
 * ```
 *
 * @param target The node to redirect to.
 * @param block The routing context configuration.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.routing(target: CommandNode<S>, block: KtCommandRoutingContext<S>. () -> Unit) {
    redirect(target) { context ->
        val routingContext = KtCommandRoutingContext(context)
        try {
            routingContext.block()
        } catch (e: Throwable) {
            routingContext.onException()
            throw e
        } finally {
            routingContext.close()
        }
        context.source
    }
}
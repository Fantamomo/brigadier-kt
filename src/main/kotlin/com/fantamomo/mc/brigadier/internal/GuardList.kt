package com.fantamomo.mc.brigadier.internal

import com.fantamomo.mc.brigadier.GuardResult
import com.fantamomo.mc.brigadier.KtCommandContext
import com.fantamomo.mc.brigadier.KtCommandGuard

/**
 * Represents a linked list of guards attached to command nodes.
 *
 * This structure allows executing guards in a deterministic order
 * from the root node to the current command node.
 *
 * @param S The command source type.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
sealed interface GuardList<S> {

    /**
     * The guard assigned to this node.
     */
    var value: KtCommandGuard<S>

    /**
     * Determines whether this guard should execute when the
     * execution target is exactly this node.
     */
    var runOnSameNode: Boolean

    /**
     * Represents the first guard in the chain.
     */
    data class First<S>(
        override var value: KtCommandGuard<S> = KtCommandGuard.default(),
        override var runOnSameNode: Boolean = true
    ) : GuardList<S>

    /**
     * Represents a further guard in the chain.
     *
     * @param previous The previous guard in the chain.
     */
    data class Next<S>(
        val previous: GuardList<S>,
        override var value: KtCommandGuard<S> = KtCommandGuard.default(),
        override var runOnSameNode: Boolean = true
    ) : GuardList<S>

    /**
     * Creates the next guard node in the chain.
     */
    fun next(): GuardList<S> = Next(this)

    /**
     * Executes all guards in this chain in correct order. (first to last)
     *
     * @param context The command context.
     * @return The resulting [GuardResult].
     */
    fun execute(context: KtCommandContext<S>): GuardResult =
        executeRecursive(context, this)

    private fun executeRecursive(
        context: KtCommandContext<S>,
        end: GuardList<S>
    ): GuardResult = when (this) {
        is First -> executeCurrent(context, end)
        is Next -> {
            val previousResult = previous.executeRecursive(context, end)
            if (previousResult != GuardResult.Continue) {
                previousResult
            } else {
                executeCurrent(context, end)
            }
        }
    }

    private fun executeCurrent(
        context: KtCommandContext<S>,
        end: GuardList<S>
    ) = if (this !== end || runOnSameNode) {
        value.runGuard(context)
    } else {
        GuardResult.Continue
    }

    /**
     * Determines if the current guard chain contains any custom-defined guard.
     *
     * A custom guard is any guard that is not the default no-op guard provided by [KtCommandGuard.default].
     *
     * @return `true` if a custom guard exists in the chain; `false` otherwise.
     */
    fun hasCustomGuard(): Boolean = hasCustomGuard(this)

    companion object {
        private tailrec fun hasCustomGuard(ref: GuardList<*>): Boolean {
            if (ref.value !== KtCommandGuard.default<Any?>()) return true
            return when (ref) {
                is First -> false
                is Next -> hasCustomGuard(ref.previous)
            }
        }
    }
}
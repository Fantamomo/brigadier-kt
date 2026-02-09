package com.fantamomo.mc.brigadier

/**
 * Represents a middleware-like guard that is executed before a command's
 * [execute] block.
 *
 * A [KtCommandGuard] is part of the command execution pipeline and allows
 * performing validation, argument transformation, permission checks, or any
 * other pre-execution logic.
 *
 * Guards are executed in order from the root node down to the currently
 * executed command node. If a guard returns [GuardResult.Abort], execution
 * stops immediately and the returned result code is propagated.
 *
 * Guards operate on [KtCommandContext], which allows:
 * - Reading parsed arguments
 * - Overriding arguments
 * - Injecting new arguments
 * - Aborting or continuing execution
 *
 * @param S The type of the command source.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
@KtCommandDsl
fun interface KtCommandGuard<S> {

    /**
     * Executes this guard within the given [KtCommandContext].
     *
     * @receiver The wrapped command context.
     * @return A [GuardResult] indicating whether execution should continue
     *         or abort.
     */
    fun KtCommandContext<S>.run(): GuardResult

    /**
     * Executes this guard for the provided [context].
     *
     * This method is used internally to invoke the guard in a structured
     * and predictable manner.
     *
     * @param context The current command context.
     * @return The [GuardResult] returned by this guard.
     */
    fun runGuard(context: KtCommandContext<S>): GuardResult = context.run()

    /**
     * A default no-op guard that always continues execution.
     */
    object Continue : KtCommandGuard<Any?> {
        override fun KtCommandContext<Any?>.run() = GuardResult.Continue
    }

    companion object {

        /**
         * Returns a default guard instance that always continues execution.
         *
         * This is used internally to initialize guard chains.
         *
         * @param S The command source type.
         */
        @Suppress("UNCHECKED_CAST")
        fun <S> default(): KtCommandGuard<S> =
            Continue as KtCommandGuard<S>
    }
}

/**
 * Represents the result of a guard execution.
 *
 * Guards return a [GuardResult] to explicitly control the execution flow.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
sealed interface GuardResult {

    /**
     * Indicates that command execution should continue normally.
     */
    data object Continue : GuardResult

    /**
     * Indicates that command execution should be aborted.
     *
     * @param result The integer result code that should be returned
     *               to Brigadier.
     */
    data class Abort(val result: Int = 0) : GuardResult
}

/**
 * Registers a [KtCommandGuard] on this command builder.
 *
 * The guard will be executed before the [execute] block when
 * this node (or one of its children) is invoked.
 *
 * Guards are executed from the root node down to the current node.
 *
 * @param runOnSameNode Whether the guard should run when this [KtCommandBuilder] executes
 * @param guard The guard to register.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.guard(
    runOnSameNode: Boolean = true,
    guard: KtCommandGuard<S>
) {
    guards.value = guard
    guards.runOnSameNode = runOnSameNode
}

/**
 * Convenience function to continue command execution from within a guard.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
fun KtCommandContext<*>.continueCommand() =
    GuardResult.Continue

/**
 * Convenience function to abort command execution from within a guard.
 *
 * @param result The result code to return to Brigadier.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
fun KtCommandContext<*>.abort(result: Int = 0) =
    GuardResult.Abort(result)
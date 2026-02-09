package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext

/**
 * Registers the execution logic for this command node.
 *
 * If [runGuards] is `true` (the default), the execution logic is as followed:
 *
 * - Before invoking the provided [block], all guards in the
 * current execution path are executed.
 * - If a guard returns [GuardResult.Abort], execution stops and the
 * provided result code is returned to Brigadier.
 * - If all guards return [GuardResult.Continue], the execution block
 * is invoked.
 *
 * If [runGuards] is `false`, the provided [block] is executed without executing any guard.
 *
 * @param block The command execution logic.
 * @param runGuards Whether to run guards before executing the block.
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.execute(runGuards: Boolean = true, block: @KtCommandDsl CommandContext<S>.() -> Int) {
    if (!runGuards) {
        builder.executes(block)
        return
    }
    val guards = guards
    builder.executes { ctx ->
        if (!guards.hasCustomGuard()) return@executes block(ctx)
        val context = KtCommandContext.of(ctx)
        val result = guards.execute(context)
        if (result is GuardResult.Abort) return@executes result.result
        block(context)
    }
}
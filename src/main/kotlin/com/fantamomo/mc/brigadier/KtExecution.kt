package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext

/**
 * Registers the execution logic for this command node.
 *
 * Before invoking the provided [block], all guards in the
 * current execution path are executed.
 *
 * If a guard returns [GuardResult.Abort], execution stops and the
 * provided result code is returned to Brigadier.
 *
 * If all guards return [GuardResult.Continue], the execution block
 * is invoked.
 *
 * @param block The command execution logic.
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.execute(block: @KtCommandDsl CommandContext<S>.() -> Int) {
    val guards = guards
    builder.executes { ctx ->
        val context = KtCommandContext.of(ctx)
        val result = guards.execute(context)
        if (result is GuardResult.Abort) return@executes result.result
        block(context)
    }
}
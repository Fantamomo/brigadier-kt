package com.fantamomo.mc.brigadier

import com.fantamomo.mc.brigadier.internal.GuardList
import com.mojang.brigadier.Command
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
        val command = RoutingCommand(block)
        builder.executes(command)
        return
    }
    val command = GuardedCommand(guards, block)
    builder.executes(command)
}

/**
 * A command implementation that applies routing context before execution.
 *
 * If routing data is present, the incoming [CommandContext] is wrapped into
 * a [KtCommandContext] so that routing values can be accessed.
 *
 * @author Fantamomo
 * @since 1.6-SNAPSHOT
 */
private class RoutingCommand<S>(private val block: CommandContext<S>.() -> Int) : Command<S> {
    override fun run(ctx: CommandContext<S>): Int {
        val routingContext = KtCommandRoutingStorage.getForExecution(ctx) ?: return block(ctx)

        @Suppress("UNCHECKED_CAST")
        val context = KtCommandContext.of(ctx, routingContext as MutableMap<KtRoutingKey<S, *>, Any?>?)
        return block(context)
    }
}

/**
 * A command implementation that executes Guards before the actual command logic.
 *
 * If routing data is present, the context is wrapped into [KtCommandContext]
 * so both routing values and argument mutations are available.
 *
 * Guards are executed from root to leaf before invoking the command block.
 *
 * @author Fantamomo
 * @since 1.5-SNAPSHOT
 */
private class GuardedCommand<S>(
    private val guards: GuardList<S>,
    private val block: CommandContext<S>.() -> Int,
) : Command<S> {
    private val resolvedGuards: Array<GuardList<S>>? by lazy {
        val end = guards
        val nodes = ArrayDeque<GuardList<S>>()
        var current: GuardList<S> = guards
        while (true) {
            nodes.addFirst(current)
            current = if (current is GuardList.Next) current.previous else break
        }
        if (!end.runOnSameNode) nodes.removeLast()
        val arr = nodes.toTypedArray()
        if (arr.isEmpty()) null else arr
    }

    override fun run(ctx: CommandContext<S>): Int {
        val routingContext = KtCommandRoutingStorage.getForExecution(ctx)
        val resolvedGuards = resolvedGuards
        if (resolvedGuards == null && routingContext == null) return block(ctx)
        @Suppress("UNCHECKED_CAST")
        val context = KtCommandContext.of(ctx, routingContext as MutableMap<KtRoutingKey<S, *>, Any?>?)
        if (resolvedGuards == null) return block(context)
        for (node in resolvedGuards) {
            val result = node.value.runGuard(context)
            if (result is GuardResult.Abort) return result.result
        }
        return block(context)
    }
}
package com.fantamomo.mc.brigadier

import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.SingleRedirectModifier
import com.mojang.brigadier.tree.CommandNode

/**
 * Configures the redirection of a command node to another target node.
 *
 * This method allows you to specify a redirect behavior for a command,
 * enabling the command to transfer its execution flow to a different target node.
 * Optionally, a custom [modifier] can be supplied to dynamically determine
 * the redirection target based on the command context.
 *
 * @param S The type of the command source (e.g., player, console).
 * @param target The target command node to which this node will redirect.
 * @param modifier An optional lambda to modify the redirection behavior.
 * If supplied, it allows dynamic alteration of the redirection target
 * based on the command context. Defaults to `null` for static redirection.
 *
 * @author Fantamomo
 * @since 1.5-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.redirect(target: CommandNode<S>, modifier: SingleRedirectModifier<S>? = null) {
    builder.redirect(target, modifier)
}

/**
 * Redirects the execution of a command to a different target node with a specified modifier function.
 *
 * This method modifies the command structure by forking its execution path to the specified [target]
 * node. The [modifier] function determines the redirection logic by mapping the original context
 * to a new set of contexts for the target node.
 *
 * @param target The command node to which the execution will be redirected.
 * @param modifier A function specifying the redirection logic, mapping the current context
 *                 to a collection of contexts for the target node.
 *
 * @author Fantamomo
 * @since 1.5-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.fork(target: CommandNode<S>, modifier: RedirectModifier<S>) {
    builder.fork(target, modifier)
}

/**
 * Configures a forwarding behavior for the current command node.
 *
 * This method sets up a redirection for the current command node to the specified
 * target node. Commands directed to the current node will be forwarded to the
 * provided [target] node. Optionally, a [modifier] can be supplied to alter the
 * context during redirection, and [fork] can be used to enable or disable forking,
 * allowing multiple command nodes to be executed in parallel.
 *
 * @param target The target command node to which execution is forwarded.
 * @param fork Indicates whether the forwarding supports forking to multiple targets.
 * @param modifier A function that modifies the context during the forwarding process.
 *
 * @author Fantamomo
 * @since 1.5-SNAPSHOT
 */
fun <S> KtCommandBuilder<S, *>.forward(target: CommandNode<S>, fork: Boolean, modifier: RedirectModifier<S>) {
    builder.forward(target, modifier, fork)
}
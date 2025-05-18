package com.fantamomo.mc.brigadier

import com.mojang.brigadier.tree.CommandNode

fun <S> command(literal: String, block: KtLiteralCommandBuilder<S>.() -> Unit): CommandNode<S> {
    val builder = KtLiteralCommandBuilder<S>(literal)
    builder.block()
    return builder.build()
}
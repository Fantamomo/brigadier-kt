package com.fantamomo.mc.brigadier

import com.mojang.brigadier.context.CommandContext

fun <S> KtCommandBuilder<S, *>.execute(block: @KtCommandDsl CommandContext<S>.() -> Int) {
    builder.executes(block)
}
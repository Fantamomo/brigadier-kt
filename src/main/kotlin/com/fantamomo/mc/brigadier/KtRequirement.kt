package com.fantamomo.mc.brigadier

fun <S> KtCommandBuilder<S, *>.requires(block: @KtCommandDsl S.() -> Boolean) {
    builder.requires(block)
}
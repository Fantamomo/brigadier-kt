package com.fantamomo.mc.brigadier

import com.mojang.brigadier.arguments.ArgumentType

fun <S> KtCommandBuilder<S, *>.literal(literal: String, block: KtLiteralCommandBuilder<S>.() -> Unit) {
    val literalBuilder = KtLiteralCommandBuilder<S>(literal)
    literalBuilder.block()
    builder.then(literalBuilder.build())
}

fun <S, T> KtCommandBuilder<S, *>.argument(name: String, type: ArgumentType<T>, block: KtArgumentCommandBuilder<S, T>.() -> Unit) {
    val argumentBuilder = KtArgumentCommandBuilder<S, T>(name, type)
    argumentBuilder.block()
    builder.then(argumentBuilder.build())
}
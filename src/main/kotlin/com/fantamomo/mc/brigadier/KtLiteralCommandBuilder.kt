package com.fantamomo.mc.brigadier

import com.mojang.brigadier.builder.LiteralArgumentBuilder

class KtLiteralCommandBuilder<S>(literal: String) : KtCommandBuilder<S, LiteralArgumentBuilder<S>>() {
    override val builder: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(literal)
}
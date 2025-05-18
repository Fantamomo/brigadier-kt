package com.fantamomo.mc.brigadier

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.tree.CommandNode

@KtCommandDsl
abstract class KtCommandBuilder<S, B : ArgumentBuilder<S, B>> {
    internal abstract val builder: B

    fun build(): CommandNode<S> = builder.build()
}
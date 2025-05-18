package com.fantamomo.mc.brigadier

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder

class KtArgumentCommandBuilder<S, T>(name: String, type: ArgumentType<T>) : KtCommandBuilder<S, RequiredArgumentBuilder<S, T>>() {
    override val builder: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(name, type)
}
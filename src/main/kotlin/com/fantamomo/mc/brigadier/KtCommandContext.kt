package com.fantamomo.mc.brigadier

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import kotlin.reflect.KClass

@Suppress("UnusedReceiverParameter")
val CommandContext<*>.SINGLE_SUCCESS: Int get() = Command.SINGLE_SUCCESS

fun <T : Any> CommandContext<*>.arg(kclass: KClass<T>, name: String): T =
    this.getArgument(name, kclass.javaPrimitiveType ?: kclass.java)

fun <T : Any> CommandContext<*>.optionalArg(kclass: KClass<T>, name: String): T? = try {
    arg(kclass, name)
} catch (e: IllegalArgumentException) {
    null
}

inline fun <reified T : Any> CommandContext<*>.arg(name: String): T = arg(T::class, name)

inline fun <reified T : Any> CommandContext<*>.optionalArg(name: String): T? = optionalArg(T::class, name)
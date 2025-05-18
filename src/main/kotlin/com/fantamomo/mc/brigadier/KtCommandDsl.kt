package com.fantamomo.mc.brigadier

/**
 * Defines a DSL marker for the Kotlin-based command DSL.
 *
 * This annotation is used to scope the command DSL, ensuring that DSL blocks
 * are structured in a clear and confined manner, preventing ambiguities or
 * misuse when mixing different DSL scopes. It helps enforce a correct usage
 * of DSL by restricting the invocation of external builders within the same
 * scope.
 *
 * Classes or methods annotated with `@KtCommandDsl` are part of the Brigadier-based
 * command-building system for creating Minecraft-style commands in Kotlin.
 *
 * The annotation should only be applied to types and is retained in the source
 * code.
 *
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class KtCommandDsl

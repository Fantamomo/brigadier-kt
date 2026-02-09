package com.fantamomo.mc.brigadier.internal

/**
 * Represents a symbolic entity with a name identifier.
 *
 * This class is designed to ensure symbol uniqueness by overriding
 * the [equals] method to use reference equality. This implies
 * that two symbols will only be considered equal if they refer
 * to the exact same instance.
 *
 * Additionally, the [hashCode] and [toString] methods depend solely
 * on the [name] property to facilitate debugging and hashing operations.
 *
 * @param name The name of the symbol, used as its identifier.
 *
 * @author Fantamomo
 * @since 1.2-SNAPSHOT
 */
internal data class Symbol(val name: String) {
    override fun equals(other: Any?) = this === other
    override fun hashCode() = name.hashCode()
    override fun toString() = "Symbol($name)"
}
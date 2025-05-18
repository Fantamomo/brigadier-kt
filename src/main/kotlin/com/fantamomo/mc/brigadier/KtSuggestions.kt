package com.fantamomo.mc.brigadier

import com.mojang.brigadier.Message
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

/**
 * A utility class for building suggestions within the context of a command execution.
 *
 * The `KtSuggestionsBuilder` is designed to work with the Brigadier command framework,
 * providing a clear and concise way to define and manage command suggestions.
 * It leverages the `SuggestionsBuilder` from Brigadier while integrating with
 * the Kotlin DSL for enhanced readability and usage.
 *
 * @param S The type of the command source (e.g., player, console).
 * @property context The command execution context for which suggestions are being built.
 * @property builder The underlying `SuggestionsBuilder` used to create the suggestion results.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
@KtCommandDsl
class KtSuggestionsBuilder<S>(val context: CommandContext<S>, val builder: SuggestionsBuilder)

/**
 * Adds a suggestion to the current suggestion builder.
 *
 * This method allows adding a single suggestion string to the underlying `SuggestionsBuilder`,
 * enabling dynamic and intuitive autocomplete functionality in command execution contexts.
 *
 * @param suggestion The suggestion string to add to the builder.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun KtSuggestionsBuilder<*>.suggest(suggestion: String) {
    builder.suggest(suggestion)
}

/**
 * Adds a suggestion with an associated tooltip to the suggestions builder.
 *
 * This method allows specifying a suggestion string that will be displayed
 * to the user along with a tooltip message for additional context or explanation.
 *
 * @param suggestion The suggestion text to present to the user.
 * @param tooltip A message providing additional details or context for the suggestion.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun KtSuggestionsBuilder<*>.suggest(suggestion: String, tooltip: Message) {
    builder.suggest(suggestion, tooltip)
}

/**
 * Adds a suggestions provider for the current argument-based command.
 *
 * This method allows defining possible suggestions for the argument being built.
 * The suggestions are created within the provided DSL block, where developers
 * can use the `KtSuggestionsBuilder` to define and manage suggestion entries
 * in a clear and concise manner.
 *
 * @param block A DSL block where the suggestions for the current command argument
 *              are defined. The block is executed in the context of `KtSuggestionsBuilder<S>`,
 *              which provides methods to manage suggestions.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtArgumentCommandBuilder<S, *>.suggests(block: KtSuggestionsBuilder<S>.() -> Unit) {
    builder.suggests { context, builder ->
        val suggestions = KtSuggestionsBuilder(context, builder)
        suggestions.block()
        suggestions.builder.buildFuture()
    }
}

/**
 * Overrides the default suggestions for an argument in a command.
 *
 * This method allows specifying a custom suggestion provider for the argument,
 * which dynamically generates suggestions based on the given implementation
 * of the provided block.
 *
 * @param block A lambda defining the custom suggestions provider.
 *              It operates within the scope of `CommandContext<S>` and uses
 *              a `SuggestionsBuilder` to collect suggestions, returning
 *              a `CompletableFuture<Suggestions>` result containing the
 *              generated suggestions.
 * @param S The type representing the source of the command context.
 * @author Fantamomo
 * @since 1.0-SNAPSHOT
 */
fun <S> KtArgumentCommandBuilder<S, *>.suggestsOverride(
    block: @KtCommandDsl CommandContext<S>.(builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>,
) {
    builder.suggests(block)
}
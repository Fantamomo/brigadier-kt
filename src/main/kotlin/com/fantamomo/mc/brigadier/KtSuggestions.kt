package com.fantamomo.mc.brigadier

import com.mojang.brigadier.Message
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture

@KtCommandDsl
class KtSuggestionsBuilder<S>(val context: CommandContext<S>, val builder: SuggestionsBuilder)

fun KtSuggestionsBuilder<*>.suggest(suggestion: String) {
    builder.suggest(suggestion)
}

fun KtSuggestionsBuilder<*>.suggest(suggestion: String, tooltip: Message) {
    builder.suggest(suggestion, tooltip)
}

fun <S> KtArgumentCommandBuilder<S, *>.suggests(block: KtSuggestionsBuilder<S>.() -> Unit) {
    builder.suggests { context, builder ->
        val suggestions = KtSuggestionsBuilder(context, builder)
        suggestions.block()
        suggestions.builder.buildFuture()
    }
}

fun <S> KtArgumentCommandBuilder<S, *>.suggestsOverride(
    block: @KtCommandDsl CommandContext<S>.(builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>,
) {
    builder.suggests(block)
}
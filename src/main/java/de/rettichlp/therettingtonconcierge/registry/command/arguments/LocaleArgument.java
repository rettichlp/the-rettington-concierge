package de.rettichlp.therettingtonconcierge.registry.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static java.util.Locale.availableLocales;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;

public class LocaleArgument implements CustomArgumentType<Locale, String> {

    private static final DynamicCommandExceptionType ERROR_ENUM_NOT_FOUND = new DynamicCommandExceptionType(
            value -> Component.translatableEscape("argument.enum.invalid", value)
    );

    private final List<Locale> elements;

    public LocaleArgument() {
        this.elements = availableLocales().toList();
    }

    public @NonNull Locale parse(@NonNull StringReader stringReader) throws CommandSyntaxException {
        String arg = stringReader.readUnquotedString();
        return this.elements.stream()
                .filter(locale -> locale.toLanguageTag().equals(arg))
                .findFirst()
                .orElseThrow(() -> ERROR_ENUM_NOT_FOUND.create(arg));
    }

    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return word();
    }

    @Override
    public @NonNull <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context,
                                                                       @NonNull SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider
                ? suggest(this.elements.stream().map(Locale::toLanguageTag).toList(), builder)
                : empty();
    }

    public static @NonNull LocaleArgument locale() {
        return new LocaleArgument();
    }
}

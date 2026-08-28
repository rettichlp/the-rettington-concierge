package de.rettichlp.therettingtonconcierge.registry.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.papermc.paper.command.brigadier.MessageComponentSerializer.message;
import static java.lang.String.valueOf;
import static java.util.Locale.ROOT;
import static java.util.Locale.availableLocales;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class LocaleArgument implements CustomArgumentType<Locale, String> {

    private static final DynamicCommandExceptionType ERROR_ENUM_NOT_FOUND = new DynamicCommandExceptionType(
            value -> message().serialize(translatable("argument.enum.invalid", text(valueOf(value))))
    );

    private final List<Locale> elements;

    public LocaleArgument() {
        this.elements = availableLocales().toList();
    }

    @Override
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
        if (!(context.getSource() instanceof CommandSourceStack)) {
            return builder.buildFuture();
        }

        this.elements.stream()
                .map(Locale::toLanguageTag)
                .filter(tabString -> tabString.toLowerCase(ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public static @NonNull LocaleArgument locale() {
        return new LocaleArgument();
    }
}

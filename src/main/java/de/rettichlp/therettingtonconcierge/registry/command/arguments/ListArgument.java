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
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static com.mojang.brigadier.suggestion.Suggestions.empty;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;

public class ListArgument<T extends ICommandArgument> implements CustomArgumentType<T, String> {

    private static final DynamicCommandExceptionType ERROR_ENUM_NOT_FOUND = new DynamicCommandExceptionType(
            value -> Component.translatableEscape("argument.enum.invalid", value)
    );

    private final List<T> elements;

    /**
     * Constructs a new {@link ListArgument} instance with the specified list of elements. The elements provided must implement the
     * {@link ICommandArgument} interface and define valid inputs as well as their corresponding tab completion strings.
     *
     * @param elements A list of elements implementing the {@link ICommandArgument} interface. These elements define the valid inputs
     *                 and the corresponding tab completion suggestions.
     */
    public ListArgument(List<T> elements) {
        this.elements = elements;
    }

    @Override
    public @NonNull T parse(@NonNull StringReader stringReader) throws CommandSyntaxException {
        String arg = stringReader.readUnquotedString();
        return this.elements.stream()
                .filter(t -> t.getTabString().equalsIgnoreCase(arg))
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
                ? suggest(this.elements.stream().map(ICommandArgument::getTabString).toList(), builder)
                : empty();
    }

    /**
     * Creates a new instance of {@link ListArgument} with the provided list of command argument elements.
     *
     * @param <T>      The type of the command argument, which extends {@link ICommandArgument}.
     * @param elements A list of elements implementing the {@link ICommandArgument} interface. These elements define the valid inputs
     *                 and their corresponding tab completion strings.
     *
     * @return A new instance of {@link ListArgument} initialized with the provided elements.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <T extends ICommandArgument> @NonNull ListArgument<T> listArgument(List<T> elements) {
        return new ListArgument<>(elements);
    }
}

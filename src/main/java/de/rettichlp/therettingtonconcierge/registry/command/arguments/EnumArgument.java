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
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static io.papermc.paper.command.brigadier.MessageComponentSerializer.message;
import static java.lang.String.valueOf;
import static java.util.Arrays.stream;
import static java.util.Locale.ROOT;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class EnumArgument<E extends Enum<E> & ICommandArgument> implements CustomArgumentType<E, String> {

    private static final DynamicCommandExceptionType ERROR_ENUM_NOT_FOUND = new DynamicCommandExceptionType(
            value -> message().serialize(translatable("argument.enum.invalid", text(valueOf(value))))
    );

    private final Class<E> enumType;

    /**
     * Constructs a new {@link EnumArgument} instance to handle parsing and validating arguments against the specified enum type. The
     * enum type must extend {@link Enum} and implement the {@link ICommandArgument} interface.
     *
     * @param enumType The class of the enum type to use for argument parsing and validation.
     */
    public EnumArgument(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public @NonNull E parse(@NonNull StringReader stringReader) throws CommandSyntaxException {
        String arg = stringReader.readUnquotedString();
        return stream(this.enumType.getEnumConstants())
                .filter(enumConstant -> enumConstant.getTabString().equalsIgnoreCase(arg))
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

        stream(this.enumType.getEnumConstants())
                .map(ICommandArgument::getTabString)
                .filter(tabString -> tabString.toLowerCase(ROOT).startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    /**
     * Creates a new {@link EnumArgument} instance for the specified enum type. The enum type must extend {@link Enum} and implement
     * the {@link ICommandArgument} interface.
     *
     * @param <E>      The type of the enum, extending {@link Enum} and implementing {@link ICommandArgument}.
     * @param enumType The class of the enum type to create the argument for.
     *
     * @return A new {@link EnumArgument} instance for the specified enum type.
     */
    @Contract(value = "_ -> new", pure = true)
    public static <E extends Enum<E> & ICommandArgument> @NonNull EnumArgument<E> enumArgument(Class<E> enumType) {
        return new EnumArgument<>(enumType);
    }
}

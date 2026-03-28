package de.rettichlp.therettingtonconcierge.registry.command.internal;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.rettichlp.therettingtonconcierge.registry.command.Command;
import de.rettichlp.therettingtonconcierge.registry.command.ICommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import static com.google.common.collect.Lists.newArrayList;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static java.util.Optional.ofNullable;

@ApiStatus.Internal
public final class CommandFactory {

    private final ICommand internalCommand;

    private CommandFactory(@NonNull ICommand internalCommand) {
        this.internalCommand = internalCommand;
    }

    /**
     * Register the command into the command registrar of PaperMC's brigadier implementation.
     *
     * @param registrar to put the command in.
     *
     * @return the created command node
     */
    public @NonNull Command printIntoCommandRegistrar(@NonNull Commands registrar) {
        final Command command = getCommands();

        final String label = command.label();
        final String[] aliases = command.aliases();
        final LiteralArgumentBuilder<CommandSourceStack> literal = literal(label);

        registrar.register(this.internalCommand.node(literal).build(), newArrayList(aliases));
        return command;
    }

    /**
     * Read all annotations a command class could possibly have.
     *
     * @return a {@link Command} which has all information about the command.
     */
    private @NonNull Command getCommands() {
        final Class<? extends ICommand> commandClass = this.internalCommand.getClass();
        return ofNullable(commandClass.getAnnotation(Command.class)).orElseThrow(() -> new RuntimeException("Failed to create instance for command " + commandClass.getName() + ". No @Command annotation found."));
    }

    /**
     * Creates a new instance of {@link CommandFactory} using the given {@link ICommand}. This factory is responsible for managing and
     * handling command-related operations.
     *
     * @param internalCommand the command to be encapsulated within the {@link CommandFactory}. It must implement the {@link ICommand}
     *                        interface.
     *
     * @return a new instance of {@link CommandFactory} initialized with the provided {@link ICommand}.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NonNull CommandFactory commandFactory(@NonNull ICommand internalCommand) {
        return new CommandFactory(internalCommand);
    }
}

package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory.commandFactory;
import static io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS;

@Singleton
public final class CommandRegistry extends AbstractRegistry<ICommand> {

    @Inject
    public CommandRegistry(@NonNull JavaPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, ICommand.class, "command");
    }

    @Override
    public @NonNull @Unmodifiable List<Class<ICommand>> classes() {
        return getAllClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Command.class))
                .toList();
    }

    @Override
    public void register(Class<ICommand> clazz, ICommand instance) {
        this.plugin.getLifecycleManager().registerEventHandler(COMMANDS, event -> {
            CommandFactory commandFactory = commandFactory(instance);
            commandFactory.printIntoCommandRegistrar(event.registrar());
        });
    }
}

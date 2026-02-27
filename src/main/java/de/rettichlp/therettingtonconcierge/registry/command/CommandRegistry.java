package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.classloader.ProtectedClassLoaderAccessor;
import de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.atteo.classindex.ClassIndex;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory.commandFactory;
import static io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableCollection;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

@Singleton
public final class CommandRegistry {

    private final List<@NonNull Command> commands = newArrayList();
    private final CommandMap commandMap;
    private final Plugin plugin;
    private final LifecycleEventManager<@NonNull Plugin> lifecycleEventManager;
    private final Injector injector;

    /**
     * Constructs a new instance of {@link CommandRegistry}, responsible for managing command registration and lifecycle events.
     *
     * @param commandMap            the map where commands are registered. This must not be null.
     * @param plugin                the plugin instance associated with this command registry. This must not be null.
     * @param lifecycleEventManager the manager responsible for handling lifecycle events such as initialization and shutdown. This
     *                              must not be null.
     * @param injector              the dependency injection framework instance used for resolving dependencies. This must not be
     *                              null.
     */
    @Inject
    public CommandRegistry(@NonNull CommandMap commandMap,
                           @NonNull Plugin plugin,
                           @SuppressWarnings("rawtypes") /* Need to remove type parameter due to dependency injection */ @NonNull LifecycleEventManager lifecycleEventManager,
                           @NonNull Injector injector) {
        this.commandMap = commandMap;
        this.plugin = plugin;
        this.injector = injector;

        //noinspection unchecked
        this.lifecycleEventManager = lifecycleEventManager;
    }

    /**
     * Register all commands available in the project which is using this library.
     */
    public void registerAllCommands() {
        if (!(this.plugin instanceof ProtectedClassLoaderAccessor accessor)) {
            this.plugin.getLogger().log(WARNING, "Cannot register commands. Plugin main is not implementing {}.", ProtectedClassLoaderAccessor.class.getName());
            return;
        }

        this.lifecycleEventManager.registerEventHandler(COMMANDS, event -> {
            int skipped = 0;

            Iterable<Class<? extends ICommand>> subclasses = ClassIndex.getSubclasses(ICommand.class, accessor.accessProtectedClassLoader());
            for (Class<? extends ICommand> commandClass : subclasses) {
                try {
                    CommandFactory commandFactory = commandFactory(this.injector.getInstance(commandClass));
                    final Command command = commandFactory.printIntoCommandRegistrar(event.registrar());

                    if (command.disabled()) {
                        skipped++;
                    } else {
                        this.commands.add(command);
                    }
                } catch (Exception e) {
                    this.plugin.getLogger().log(WARNING, "Failed to register command " + commandClass.getSimpleName() + ": " + e.getMessage(), e);
                }
            }

            this.plugin.getLogger().log(INFO, "Registered commands: " + this.commands.size() + "/" + Iterables.size(subclasses) + ", " + skipped + " skipped");
        });
    }

    /**
     * Unregister a specific command by its label and aliases.
     *
     * @param command to unregister.
     */
    public void unregister(@NonNull Command command) {
        stream(command.aliases()).forEach(this.commandMap.getKnownCommands()::remove);
        this.commandMap.getKnownCommands().remove(command.label());
        this.plugin.getLogger().log(INFO, "Removed command " + command.label() + ".");
    }

    /**
     * Retrieves an unmodifiable view of the collection of registered command nodes. These command nodes represent the metadata and
     * configurations for commands currently registered in the system.
     *
     * @return an unmodifiable {@link Collection} of {@link Command}, providing details about all commands managed by this
     *         registry.
     */
    public @NonNull @UnmodifiableView Collection<Command> commands() {
        return unmodifiableCollection(this.commands);
    }
}

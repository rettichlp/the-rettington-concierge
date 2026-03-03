package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import de.rettichlp.therettingtonconcierge.registry.Ignore;
import de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Predicate;

import static de.rettichlp.therettingtonconcierge.registry.command.internal.CommandFactory.commandFactory;
import static io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS;

@Singleton
public final class CommandRegistry extends AbstractRegistry {

    @Inject
    public CommandRegistry(@NonNull JavaPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, "command");
    }

    @Override
    public @NonNull @Unmodifiable List<Class<?>> getClasses() {
        return this.classes.stream()
                .filter(clazz -> clazz.isAnnotationPresent(Command.class))
                .toList();
    }

    @Override
    public void register(Class<?> clazz) {
        this.plugin.getLifecycleManager().registerEventHandler(COMMANDS, event -> {
            ICommand instance = (ICommand) this.injector.getInstance(clazz);
            CommandFactory commandFactory = commandFactory(instance);
            commandFactory.printIntoCommandRegistrar(event.registrar());
        });
    }

    @Override
    public Predicate<Class<?>> skip() {
        return clazz -> {
            boolean isAnnotatedWithIgnore = clazz.isAnnotationPresent(Ignore.class);
            boolean isDisabled = clazz.getAnnotation(Command.class).disabled();
            return isAnnotatedWithIgnore || isDisabled;
        };
    }
}

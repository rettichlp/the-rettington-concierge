package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static java.util.Optional.ofNullable;

public abstract class AbstractCommandRegistry<T> extends AbstractRegistry<ICommand<T>> {

    @SuppressWarnings("unchecked")
    public AbstractCommandRegistry(IMinecraftPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, (Class<ICommand<T>>) (Class<?>) ICommand.class, "command");
    }

    public abstract void registerTypeSpecific(@NonNull ICommand<T> instance, String label, String[] aliases);

    @Override
    public @NonNull @Unmodifiable List<Class<ICommand<T>>> classes() {
        return getAllClasses().stream()
                .filter(clazz -> clazz.isAnnotationPresent(Command.class))
                .toList();
    }

    @Override
    public void register(@NonNull Class<ICommand<T>> clazz, ICommand<T> instance) {
        Command command = ofNullable(clazz.getAnnotation(Command.class))
                .orElseThrow(() -> new RuntimeException("Failed to create instance for command " + clazz.getSimpleName() + ": No " + Command.class.getSimpleName() + " annotation found"));

        String label = command.label();
        String[] aliases = command.aliases();

        registerTypeSpecific(instance, label, aliases);
    }
}

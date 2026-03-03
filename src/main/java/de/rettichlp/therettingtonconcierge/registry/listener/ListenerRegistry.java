package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import de.rettichlp.therettingtonconcierge.registry.Ignore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Predicate;

import static org.bukkit.Bukkit.getPluginManager;

@Singleton
public final class ListenerRegistry extends AbstractRegistry {

    @Inject
    public ListenerRegistry(@NonNull Plugin plugin, @NonNull Injector injector) {
        super(plugin, injector, "listener");
    }

    @Override
    public @NonNull @Unmodifiable List<Class<?>> getClasses() {
        return this.classes.stream()
                .filter(Listener.class::isAssignableFrom)
                .toList();
    }

    @Override
    public void register(Class<?> clazz) {
        Listener instance = (Listener) this.injector.getInstance(clazz);
        getPluginManager().registerEvents(instance, this.plugin);
    }

    @Override
    public Predicate<Class<?>> skip() {
        return clazz -> clazz.isAnnotationPresent(Ignore.class);
    }
}

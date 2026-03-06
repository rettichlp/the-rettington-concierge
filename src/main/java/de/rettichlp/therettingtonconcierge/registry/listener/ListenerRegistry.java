package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.listener.InventoryListener;
import de.rettichlp.therettingtonconcierge.listener.TRCEventListener;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import de.rettichlp.therettingtonconcierge.registry.Ignore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Predicate;

import static org.bukkit.Bukkit.getPluginManager;

@Singleton
public final class ListenerRegistry extends AbstractRegistry {

    @Inject
    public ListenerRegistry(@NonNull JavaPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, "listener");

        // register all the listener from this project
        getPluginManager().registerEvents(injector.getInstance(InventoryListener.class), this.plugin);
        getPluginManager().registerEvents(injector.getInstance(TRCEventListener.class), this.plugin);
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

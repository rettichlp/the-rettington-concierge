package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.listener.InventoryListener;
import de.rettichlp.therettingtonconcierge.listener.TRCEventListener;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;

@Singleton
public final class ListenerRegistry extends AbstractRegistry<Listener> {

    @Inject
    public ListenerRegistry(@NonNull JavaPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, Listener.class, "listener");

        // register all the listener from this project
        getPluginManager().registerEvents(injector.getInstance(InventoryListener.class), this.plugin);
        getPluginManager().registerEvents(injector.getInstance(TRCEventListener.class), this.plugin);
    }

    @Override
    public @NonNull @Unmodifiable List<Class<Listener>> classes() {
        return getAllClasses().stream()
                .filter(Listener.class::isAssignableFrom)
                .toList();
    }

    @Override
    public void register(Class<Listener> clazz, Listener instance) {
        getPluginManager().registerEvents(instance, this.plugin);
    }
}

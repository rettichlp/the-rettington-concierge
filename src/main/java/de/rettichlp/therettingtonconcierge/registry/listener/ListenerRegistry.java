package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;
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

    private final JavaPlugin javaPlugin;

    @Inject
    public ListenerRegistry(IMinecraftPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher) {
        super(plugin, injector, logDispatcher, Listener.class, "listener");

        if (!(this.plugin instanceof JavaPlugin javaPlugin)) {
            throw new IllegalStateException("Interface " + IMinecraftPlugin.class.getSimpleName() + " must be implemented by a class that also implements " + JavaPlugin.class.getSimpleName());
        }

        this.javaPlugin = javaPlugin;

        // register all the listener from this project
        getPluginManager().registerEvents(injector.getInstance(InventoryListener.class), this.javaPlugin);
        getPluginManager().registerEvents(injector.getInstance(TRCEventListener.class), this.javaPlugin);
    }

    @Override
    public @NonNull @Unmodifiable List<Class<Listener>> classes() {
        return getAllClasses().stream()
                .filter(Listener.class::isAssignableFrom)
                .toList();
    }

    @Override
    public void register(@NonNull Class<Listener> clazz, Listener instance) {
        getPluginManager().registerEvents(instance, this.javaPlugin);
    }
}

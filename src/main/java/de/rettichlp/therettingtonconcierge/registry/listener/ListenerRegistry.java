package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.classloader.ProtectedClassLoaderAccessor;
import de.rettichlp.therettingtonconcierge.listener.InventoryListener;
import de.rettichlp.therettingtonconcierge.listener.TRCEventListener;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Set;

import static com.google.common.reflect.ClassPath.from;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toSet;

@Singleton
public final class ListenerRegistry {

    private final Plugin plugin;
    private final PluginManager pluginManager;
    private final Injector injector;

    /**
     * Constructs a new ListenerRegistry instance for managing and registering event listeners.
     *
     * @param plugin        the plugin instance associated with the listener registry. Cannot be null.
     * @param pluginManager the plugin manager responsible for handling event listeners. Cannot be null.
     * @param injector      the dependency injector used to instantiate listener classes. Cannot be null.
     */
    @Inject
    public ListenerRegistry(@NonNull Plugin plugin, @NonNull PluginManager pluginManager, @NonNull Injector injector) {
        this.plugin = plugin;
        this.pluginManager = pluginManager;
        this.injector = injector;
    }

    /**
     * Registers all listeners found in the specified package(s). It utilizes the dependency injection system to instantiate listener
     * classes and registers them with the plugin manager.
     *
     * @param packageNames One or more package names to scan for classes implementing the Listener interface. Listeners found in these
     *                     packages will be registered with the plugin manager. Cannot be null.
     */
    public void registerAllListeners(@NonNull String... packageNames) {
        // register own listeners
        this.pluginManager.registerEvents(new InventoryListener(), this.plugin);
        this.pluginManager.registerEvents(new TRCEventListener(), this.plugin);

        // register sub-plugin listeners
        int skipped = 0;
        int registered = 0;

        Iterable<Class<?>> subclasses = getListenerClasses(packageNames);
        for (Class<?> listenerClass : subclasses) {
            try {
                final Listener listenerInstance = (Listener) this.injector.getInstance(listenerClass);
                // TODO: Check for ignore annotation
                registered++;
                this.pluginManager.registerEvents(listenerInstance, this.plugin);
            } catch (Exception e) {
                this.plugin.getLogger().log(WARNING, "Failed to register listener " + listenerClass.getSimpleName() + ": " + e.getMessage(), e);
            }
        }

        this.plugin.getLogger().log(INFO, "Registered listeners: " + registered + "/" + Iterables.size(subclasses) + ", " + skipped + " skipped");
    }

    /**
     * Retrieves a set of classes that implement the {@link Listener} interface and are located within the specified packages. These
     * classes must also contain at least one method annotated with {@link org.bukkit.event.EventHandler}. It utilizes a protected
     * class loader to scan for applicable classes. If the plugin does not implement {@link ProtectedClassLoaderAccessor}, an empty set
     * is returned.
     *
     * @param packageNames One or more package names to scan for classes implementing the {@link Listener} interface. Only classes
     *                     within these packages will be considered. Cannot be null.
     *
     * @return A set of classes that implement the {@link Listener} interface and are located in the specified packages. Returns an
     *         empty set if no applicable classes are found or if an error occurs.
     */
    public Set<Class<?>> getListenerClasses(@NonNull String... packageNames) {
        if (!(this.plugin instanceof ProtectedClassLoaderAccessor protectedClassLoader)) {
            this.plugin.getLogger().log(WARNING, "Cannot register listeners. Plugin main is not implementing {}.", ProtectedClassLoaderAccessor.class.getName());
            return Set.of();
        }

        // all classes with at least one method annotated with @EventHandler
        try {
            return from(protectedClassLoader.accessProtectedClassLoader()).getAllClasses().stream()
                    .filter(classInfo -> {
                        for (@NonNull String packageName : packageNames) {
                            if (classInfo.getPackageName().startsWith(packageName)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .map(ClassPath.ClassInfo::load)
                    .filter(Listener.class::isAssignableFrom)
                    .collect(toSet());
        } catch (IOException e) {
            this.plugin.getLogger().log(WARNING, "Failed to fetch listener classes: " + e.getMessage(), e);
            return Set.of();
        }
    }
}

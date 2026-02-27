package de.rettichlp.therettingtonconcierge;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import static org.bukkit.Bukkit.getCommandMap;
import static org.bukkit.Bukkit.getPluginManager;

public final class TRCGuiceBindingModule implements Module {

    private final JavaPlugin plugin;

    /**
     * A Guice module responsible for binding plugin-specific dependencies required for the operation of the RettichLP utilities. This
     * includes binding common plugin-related services such as the plugin instance, plugin manager, and command map, ensuring that
     * these dependencies are readily available for injection in other parts of the application.
     *
     * @param plugin the JavaPlugin instance representing the plugin for which this module sets up dependency injection bindings. This
     *               parameter must not be null.
     */
    public TRCGuiceBindingModule(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure(@NonNull Binder binder) {

        // set up the plugin instance
        binder.bind(Plugin.class).toInstance(this.plugin);
        binder.bind(JavaPlugin.class).toInstance(this.plugin);
        binder.bind(PluginManager.class).toInstance(getPluginManager());
        binder.bind(CommandMap.class).toInstance(getCommandMap());

        binder.bind(LifecycleEventManager.class).toInstance(this.plugin.getLifecycleManager());
    }
}

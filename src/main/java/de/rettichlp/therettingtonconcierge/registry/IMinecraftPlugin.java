package de.rettichlp.therettingtonconcierge.registry;

import com.velocitypowered.api.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public interface IMinecraftPlugin {

    /**
     * Retrieves the class loader used by the plugin. This loader allows access to the classes of the plugin, enabling actions such as
     * registering commands and listeners.
     *
     * @return the class loader associated with the plugin
     */
    @NonNull ClassLoader classloader();

    default boolean isVelocityPlugin() {
        return this.getClass().isAnnotationPresent(Plugin.class);
    }

    default boolean isPaperPlugin() {
        return this instanceof JavaPlugin;
    }

    default JavaPlugin getPaperPlugin() {
        if (!isPaperPlugin()) {
            throw new IllegalStateException("Interface " + IMinecraftPlugin.class.getSimpleName() + " must be implemented by a class that also implements " + JavaPlugin.class.getSimpleName());
        }

        return (JavaPlugin) this;
    }
}

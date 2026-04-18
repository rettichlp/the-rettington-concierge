package de.rettichlp.therettingtonconcierge.registry;

import org.jspecify.annotations.NonNull;

public interface IMinecraftPlugin {

    /**
     * Retrieves the class loader used by the plugin. This loader allows access to the classes of the plugin, enabling actions such as
     * registering commands and listeners.
     *
     * @return the class loader associated with the plugin
     */
    @NonNull ClassLoader classloader();
}

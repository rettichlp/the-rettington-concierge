package de.rettichlp.therettingtonconcierge.classloader;

import org.jspecify.annotations.NonNull;

/**
 * Provides access to the plugin's class loader, bypassing Paper's restrictions. This interface is intended as a workaround to retrieve
 * the plugin class loader, which is normally only accessible from the plugin's main class. Use with caution, as this may violate best
 * practices and compatibility guidelines.
 */
public interface ProtectedClassLoaderAccessor {

    /**
     * Retrieves the plugin's class loader.
     *
     * @return the plugin's class loader.
     */
    @NonNull ClassLoader accessProtectedClassLoader();
}

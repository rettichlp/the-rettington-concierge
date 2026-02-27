package de.rettichlp.therettingtonconcierge.utils;

import com.google.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class DevUtils {

    @Inject
    private static JavaPlugin plugin;

    public String getNiceVersion() {
        String version = getVersion();
        return version.contains("-") ? version.split("-")[0] : version;
    }

    public static @NonNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
}

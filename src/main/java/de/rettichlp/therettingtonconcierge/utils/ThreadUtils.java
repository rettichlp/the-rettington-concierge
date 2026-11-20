package de.rettichlp.therettingtonconcierge.utils;

import org.jspecify.annotations.NonNull;

import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.Bukkit.isPrimaryThread;
import static org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin;

public class ThreadUtils {

    public static void runOnMainThread(@NonNull Runnable runnable) {
        if (isPrimaryThread()) {
            runnable.run();
            return;
        }

        getScheduler().runTask(getProvidingPlugin(ThreadUtils.class), runnable);
    }
}

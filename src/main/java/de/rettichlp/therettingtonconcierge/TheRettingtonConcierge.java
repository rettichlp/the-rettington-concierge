package de.rettichlp.therettingtonconcierge;

import com.google.inject.AbstractModule;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import org.bukkit.plugin.java.JavaPlugin;

public final class TheRettingtonConcierge extends AbstractModule {

    @Override
    protected void configure() {
        // requires plugin instance for plugin depending implementations
        requireBinding(JavaPlugin.class);

        // requires a LogDispatcher instance for logging of registrations
        requireBinding(LogDispatcher.class);
    }
}

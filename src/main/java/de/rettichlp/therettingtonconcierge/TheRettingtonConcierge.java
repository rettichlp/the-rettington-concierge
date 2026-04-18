package de.rettichlp.therettingtonconcierge;

import com.google.inject.AbstractModule;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;

public final class TheRettingtonConcierge extends AbstractModule {

    @Override
    protected void configure() {
        // requires plugin instance
        requireBinding(IMinecraftPlugin.class);

        // requires a LogDispatcher instance for logging of registrations
        requireBinding(LogDispatcher.class);
    }
}

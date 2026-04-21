package de.rettichlp.therettingtonconcierge.registry.listener;

import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.registry.AbstractRegistry;
import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static java.util.Arrays.stream;

@Singleton
public final class VelocityListenerRegistry extends AbstractRegistry<Object> {

    private final ProxyServer proxyServer;

    @Inject
    public VelocityListenerRegistry(IMinecraftPlugin plugin,
                                    @NonNull Injector injector,
                                    LogDispatcher logDispatcher,
                                    ProxyServer proxyServer) {
        super(plugin, injector, logDispatcher, Object.class, "listener");
        this.proxyServer = proxyServer;
    }

    @Override
    public @NonNull @Unmodifiable List<Class<Object>> classes() {
        return getAllClasses().stream()
                .filter(objectClass -> stream(objectClass.getMethods())
                        .anyMatch(method -> method.isAnnotationPresent(Subscribe.class)))
                .toList();
    }

    @Override
    public void register(@NonNull Class<Object> clazz, Object instance) {
        this.proxyServer.getEventManager().register(this.plugin, instance);
    }
}

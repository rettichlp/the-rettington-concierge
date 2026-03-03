package de.rettichlp.therettingtonconcierge.registry;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.classloader.ProtectedClassLoaderAccessor;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.collect.Iterables.size;
import static com.google.common.reflect.ClassPath.from;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractRegistry {

    protected final JavaPlugin plugin;
    protected final Injector injector;

    protected Set<Class<?>> classes;

    private final LogDispatcher logDispatcher;
    private final String registryName;

    @Inject
    public AbstractRegistry(@NonNull JavaPlugin plugin, @NonNull Injector injector, LogDispatcher logDispatcher, String registryName) {
        this.plugin = plugin;
        this.injector = injector;
        this.logDispatcher = logDispatcher;
        this.registryName = registryName;

        this.classes = getAllClasses();
    }

    public abstract @NonNull @Unmodifiable List<Class<?>> getClasses();

    public abstract void register(Class<?> clazz);

    public abstract Predicate<Class<?>> skip();

    public void registerAll() {
        int skipped = 0;
        int registered = 0;

        Iterable<Class<?>> classes = getClasses();
        for (Class<?> clazz : classes) {
            try {
                register(clazz);
                registered++;
            } catch (Exception e) {
                this.logDispatcher.warn("Failed to register {} {}: {}", this.registryName, clazz.getSimpleName(), e.getMessage());
            }
        }

        this.logDispatcher.info("Registered {}: {}/{}, {} skipped", this.registryName, registered, size(classes), skipped);
    }

    private Set<Class<?>> getAllClasses() {
        if (!(this.plugin instanceof ProtectedClassLoaderAccessor protectedClassLoader)) {
            this.logDispatcher.warn("Cannot fetch classes. Plugin main is not implementing {}.", ProtectedClassLoaderAccessor.class.getName());
            return Set.of();
        }

        try {
            return from(protectedClassLoader.accessProtectedClassLoader()).getAllClasses().stream()
                    .filter(classInfo -> classInfo.getPackageName().startsWith("de.gronkhmc"))
                    .map(ClassPath.ClassInfo::load)
                    .collect(toSet());
        } catch (IOException e) {
            this.logDispatcher.warn("Failed to fetch classes: {}", e.getMessage());
            return Set.of();
        }
    }
}

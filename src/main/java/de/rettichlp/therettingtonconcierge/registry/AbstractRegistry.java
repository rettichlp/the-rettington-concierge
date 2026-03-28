package de.rettichlp.therettingtonconcierge.registry;

import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import de.rettichlp.therettingtonconcierge.classloader.ProtectedClassLoaderAccessor;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.size;
import static com.google.common.reflect.ClassPath.from;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractRegistry<T> {

    protected final JavaPlugin plugin;
    protected final Injector injector;

    @Getter
    protected Set<T> instances = new HashSet<>();

    private final LogDispatcher logDispatcher;
    private final Class<T> clazz;
    private final String registryName;

    @Inject
    public AbstractRegistry(@NonNull JavaPlugin plugin,
                            @NonNull Injector injector,
                            LogDispatcher logDispatcher,
                            Class<T> clazz,
                            String registryName) {
        this.plugin = plugin;
        this.injector = injector;
        this.logDispatcher = logDispatcher;
        this.clazz = clazz;
        this.registryName = registryName;
    }

    public abstract @NonNull @Unmodifiable List<Class<T>> classes();

    public abstract void register(Class<T> clazz, T instance);

    public void registerAll() {
        int skipped = 0;
        int registered = 0;

        Iterable<Class<T>> classes = classes();
        for (Class<T> clazz : classes) {
            if (clazz.isAnnotationPresent(Ignore.class)) {
                skipped++;
                continue;
            }

            try {
                T instance = this.injector.getInstance(clazz);
                register(clazz, instance);
                this.instances.add(instance);
                registered++;
            } catch (Exception e) {
                this.logDispatcher.warn("Failed to register {} {}: {}", this.registryName, clazz.getSimpleName(), e.getMessage());
            }
        }

        this.logDispatcher.info("Registered {}: {}/{}, {} skipped", this.registryName, registered, size(classes), skipped);
    }

    @SuppressWarnings("unchecked")
    protected Set<Class<T>> getAllClasses() {
        if (!(this.plugin instanceof ProtectedClassLoaderAccessor protectedClassLoader)) {
            this.logDispatcher.warn("Cannot fetch classes. Plugin main is not implementing {}.", ProtectedClassLoaderAccessor.class.getName());
            return Set.of();
        }

        try {
            return from(protectedClassLoader.accessProtectedClassLoader()).getAllClasses().stream()
                    .filter(classInfo -> classInfo.getPackageName().startsWith("de.gronkhmc"))
                    .map(ClassPath.ClassInfo::load)
                    .filter(this.clazz::isAssignableFrom)
                    .map(clazz -> (Class<T>) clazz)
                    .collect(toSet());
        } catch (IOException e) {
            this.logDispatcher.warn("Failed to fetch classes: {}", e.getMessage());
            return Set.of();
        }
    }
}

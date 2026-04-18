package de.rettichlp.therettingtonconcierge.registry;

import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin.PluginInformation.fromJavaPlugin;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

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

    default PluginInformation getPluginInformation() {
        if (isPaperPlugin()) {
            return fromJavaPlugin(getPaperPlugin());
        }

        return ofNullable(this.getClass().getAnnotation(Plugin.class))
                .map(PluginInformation::fromPluginAnnotation)
                .orElse(null);
    }

    record PluginInformation(String id, String name, String version, String description, List<String> authors, List<String> contributors, String url, List<String> dependencies) {

        public String getNiceVersion() {
            return this.version.contains("-") ? this.version.split("-")[0] : this.version;
        }

        public static @NonNull PluginInformation fromJavaPlugin(@NonNull JavaPlugin javaPlugin) {
            PluginMeta pluginMeta = javaPlugin.getPluginMeta();

            String id = javaPlugin.namespace();
            String name = javaPlugin.getName();
            String version = pluginMeta.getVersion();
            String description = pluginMeta.getDescription();
            List<String> authors = pluginMeta.getAuthors();
            List<String> contributors = pluginMeta.getContributors();
            String url = pluginMeta.getWebsite();
            List<String> dependencies = pluginMeta.getPluginDependencies();

            return new PluginInformation(id, name, version, description, authors, contributors, url, dependencies);
        }

        public static @NonNull PluginInformation fromPluginAnnotation(@NonNull Plugin plugin) {
            String id = plugin.id();
            String name = plugin.name();
            String version = plugin.version();
            String description = plugin.description();
            List<String> authors = asList(plugin.authors());
            String url = plugin.url();
            List<String> dependencies = stream(plugin.dependencies()).map(Dependency::id).toList();

            return new PluginInformation(id, name, version, description, authors, authors, url, dependencies);
        }
    }
}

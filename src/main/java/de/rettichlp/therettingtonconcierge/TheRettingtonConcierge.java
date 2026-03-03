package de.rettichlp.therettingtonconcierge;

import com.google.inject.Binder;
import com.google.inject.Module;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public final class TheRettingtonConcierge implements Module {

    public static LogDispatcher logDispatcher;

    private final JavaPlugin plugin;

    public TheRettingtonConcierge(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        initLogDispatcher();
    }

    @Override
    public void configure(@NonNull Binder binder) {
        // set up the plugin instance
        binder.bind(Plugin.class).toInstance(this.plugin);
        binder.bind(JavaPlugin.class).toInstance(this.plugin);
    }

    private void initLogDispatcher() {
        logDispatcher = LogDispatcher.builder()
                .bukkitLogger(this.plugin.getLogger())
                .prefixDebug(empty()
                        .append(text("♯", DARK_GRAY)).appendSpace()
                        .append(text("Debug", YELLOW))
                        .append(text(":", DARK_GRAY)).appendSpace())
                .prefixInfo(empty()
                        .append(text("♯", DARK_GRAY)).appendSpace()
                        .append(text("Info", BLUE))
                        .append(text(":", DARK_GRAY)).appendSpace())
                .prefixWarn(empty()
                        .append(text("♯", DARK_GRAY)).appendSpace()
                        .append(text("Warning", GOLD))
                        .append(text(":", DARK_GRAY)).appendSpace())
                .prefixError(empty()
                        .append(text("♯", DARK_GRAY)).appendSpace()
                        .append(text("Error", RED))
                        .append(text(":", DARK_GRAY)).appendSpace())
                .debugFilter(player -> player.hasPermission("log.debug"))
                .infoFilter(player -> player.hasPermission("log.info"))
                .warnFilter(player -> player.hasPermission("log.warn"))
                .errorFilter(player -> player.hasPermission("log.error"))
                .build();
    }
}

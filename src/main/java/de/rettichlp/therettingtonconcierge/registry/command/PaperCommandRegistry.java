package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.inject.Injector;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.rettichlp.therettingtonconcierge.classloader.ProtectedClassLoaderAccessor;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import static com.google.common.collect.Lists.newArrayList;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS;

@Singleton
public class PaperCommandRegistry extends AbstractCommandRegistry<CommandSourceStack> {

    private final JavaPlugin plugin;

    @Inject
    public PaperCommandRegistry(ProtectedClassLoaderAccessor protectedClassLoaderAccessor,
                                @NonNull Injector injector,
                                LogDispatcher logDispatcher,
                                @NonNull JavaPlugin plugin) {
        super(protectedClassLoaderAccessor, injector, logDispatcher);
        this.plugin = plugin;
    }

    @Override
    public void registerTypeSpecific(@NonNull ICommand<CommandSourceStack> instance, String label, String[] aliases) {
        LifecycleEventManager<Plugin> manager = this.plugin.getLifecycleManager();

        LiteralArgumentBuilder<CommandSourceStack> labelLiteral = literal(label);
        LiteralCommandNode<CommandSourceStack> commandNode = instance.node(labelLiteral).build();

        // register command
        manager.registerEventHandler(COMMANDS, event -> event.registrar().register(commandNode, newArrayList(aliases)));
    }
}

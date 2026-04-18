package de.rettichlp.therettingtonconcierge.registry.command;

import com.google.inject.Injector;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;

import static com.velocitypowered.api.command.BrigadierCommand.literalArgumentBuilder;

@Singleton
public class VelocityCommandRegistry extends AbstractCommandRegistry<CommandSource> {

    private final ProxyServer proxyServer;

    @Inject
    public VelocityCommandRegistry(IMinecraftPlugin plugin,
                                   @NonNull Injector injector,
                                   LogDispatcher logDispatcher,
                                   ProxyServer proxyServer) {
        super(plugin, injector, logDispatcher);
        this.proxyServer = proxyServer;
    }

    @Override
    public void registerTypeSpecific(@NonNull ICommand<CommandSource> instance, String label, String[] aliases) {
        if (!this.plugin.getClass().isAnnotationPresent(Plugin.class)) {
            throw new IllegalStateException("Interface " + IMinecraftPlugin.class.getSimpleName() + " must be implemented by a class that is annotated by " + Plugin.class.getName());
        }

        CommandManager manager = this.proxyServer.getCommandManager();

        LiteralArgumentBuilder<CommandSource> labelLiteral = literalArgumentBuilder(label);
        LiteralCommandNode<CommandSource> commandNode = instance.node(labelLiteral).build();

        // create command meta
        CommandMeta commandMeta = manager.metaBuilder(label)
                .aliases(aliases)
                .plugin(this.plugin)
                .build();

        // register command
        com.velocitypowered.api.command.Command brigadierCommand = new BrigadierCommand(commandNode);
        manager.register(commandMeta, brigadierCommand);
    }
}

package de.rettichlp.therettingtonconcierge.utils.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static net.minecraft.network.chat.Component.literal;
import static net.minecraft.network.chat.Component.translatable;

public class CommandUtils {

    public static Player getPlayerFromCommandSourceStack(@NonNull CommandSourceStack commandSourceStack) throws
                                                                                                         CommandSyntaxException {
        if (commandSourceStack.getSender() instanceof Player player) {
            return player;
        }

        throw new CommandSyntaxException(new SimpleCommandExceptionType(literal("test1")), literal("test2"));
    }

    public static OfflinePlayer getMentionedOfflinePlayer(@NonNull CommandContext<CommandSourceStack> commandContext) throws
                                                                                                                      CommandSyntaxException {
        return getMentionedOfflinePlayer(commandContext, "profile");
    }

    public static OfflinePlayer getMentionedOfflinePlayer(@NonNull CommandContext<CommandSourceStack> commandContext,
                                                          String argumentName) throws CommandSyntaxException {
        if (getMentionedOfflinePlayers(commandContext, argumentName).size() > 1) {
            throw new SimpleCommandExceptionType(translatable("argument.player.toomany")).create();
        }

        return getMentionedOfflinePlayers(commandContext, argumentName).getFirst();
    }

    public static @NonNull List<OfflinePlayer> getMentionedOfflinePlayers(CommandContext<CommandSourceStack> commandContext) throws
                                                                                                                             CommandSyntaxException {
        return getMentionedOfflinePlayers(commandContext, "profile");
    }

    public static @NonNull List<OfflinePlayer> getMentionedOfflinePlayers(@NonNull CommandContext<CommandSourceStack> commandContext,
                                                                          String argumentName) throws CommandSyntaxException {
        PlayerProfileListResolver playerProfileListResolver = commandContext.getArgument(argumentName, PlayerProfileListResolver.class);
        Collection<PlayerProfile> foundProfiles = playerProfileListResolver.resolve(commandContext.getSource());

        List<OfflinePlayer> offlinePlayers = foundProfiles.stream()
                .filter(playerProfile -> playerProfile.complete(false))
                .map(PlayerProfile::getId)
                .filter(Objects::nonNull)
                .map(Bukkit::getOfflinePlayer)
                .toList();

        if (offlinePlayers.isEmpty()) {
            throw new SimpleCommandExceptionType(translatable("argument.player.unknown")).create();
        }

        return offlinePlayers;
    }
}

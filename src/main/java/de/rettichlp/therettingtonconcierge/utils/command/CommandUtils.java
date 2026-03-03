package de.rettichlp.therettingtonconcierge.utils.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static net.minecraft.network.chat.Component.literal;

public class CommandUtils {

    public static Player getPlayerFromCommandSourceStack(@NonNull CommandSourceStack commandSourceStack) throws
                                                                                                         CommandSyntaxException {
        if (commandSourceStack.getSender() instanceof Player player) {
            return player;
        }

        throw new CommandSyntaxException(new SimpleCommandExceptionType(literal("test1")), literal("test2"));
    }

    public static @NonNull @Unmodifiable List<UUID> getPlayerProfiles(CommandContext<CommandSourceStack> commandContext) throws
                                                                                                                         CommandSyntaxException {
        return getPlayerProfiles(commandContext, "profile");
    }

    public static @NonNull @Unmodifiable List<UUID> getPlayerProfiles(@NonNull CommandContext<CommandSourceStack> commandContext,
                                                                      String argumentName) throws CommandSyntaxException {
        PlayerProfileListResolver playerProfileListResolver = commandContext.getArgument(argumentName, PlayerProfileListResolver.class);
        Collection<PlayerProfile> foundProfiles = playerProfileListResolver.resolve(commandContext.getSource());

        if (foundProfiles.isEmpty()) {
            throw new CommandSyntaxException(new SimpleCommandExceptionType(literal("test3")), literal("test4"));
        }

        return foundProfiles.stream()
                .filter(playerProfile -> playerProfile.complete(false))
                .map(PlayerProfile::getId)
                .toList();
    }
}

package de.rettichlp.therettingtonconcierge.utils.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PermissionUtils {

    @SafeVarargs
    public static <T> Predicate<T> oneOf(Predicate<T>... predicates) {
        return Stream.of(predicates).reduce(_ -> false, Predicate::or);
    }

    @SafeVarargs
    public static <T> Predicate<T> allOf(Predicate<T>... predicates) {
        return Stream.of(predicates).reduce(_ -> true, Predicate::and);
    }

    @Contract(pure = true)
    public static @NonNull Predicate<CommandSourceStack> isConsole() {
        return stack -> stack.getSender() instanceof ConsoleCommandSender;
    }

    @Contract(pure = true)
    public static @NonNull Predicate<CommandSourceStack> isPlayer() {
        return stack -> stack.getSender() instanceof Player;
    }

    @Contract(pure = true)
    public static @NonNull Predicate<CommandSourceStack> hasPermission(String permissionString) {
        return stack -> {
            CommandSender sender = stack.getSender();
            return sender.isOp() || sender instanceof Player player && player.hasPermission(permissionString);
        };
    }

    @Contract(pure = true)
    public static @NonNull Predicate<CommandSourceStack> has(Function<Player, Boolean> function) {
        return stack -> {
            CommandSender sender = stack.getSender();
            return sender.isOp() || sender instanceof Player player && function.apply(player);
        };
    }
}

package de.rettichlp.therettingtonconcierge.logging;

import de.rettichlp.therettingtonconcierge.registry.IMinecraftPlugin;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.bukkit.Bukkit.getOnlinePlayers;

@Log4j2
@Getter
@Builder
public class LogDispatcher {

    private Component prefixDebug;
    private Component prefixInfo;
    private Component prefixWarn;
    private Component prefixError;
    private Predicate<Player> debugFilter;
    private Predicate<Player> infoFilter;
    private Predicate<Player> warnFilter;
    private Predicate<Player> errorFilter;
    private IMinecraftPlugin plugin;
    private Logger logger;

    /**
     * Logs a debug message associated with a specific class, formats it with the provided arguments, and sends the message to online
     * players who pass the debug filter. The message is logged at the DEBUG level and includes the class name and the formatted
     * message.
     *
     * @param clazz      the class associated with the debug message
     * @param rawMessage the raw message template to be formatted with the provided arguments
     * @param args       the arguments to format the raw message
     */
    public void debug(@NonNull Class<?> clazz, String rawMessage, Object... args) {
        String formattedMessage = "[" + clazz.getSimpleName() + "] " + log.getMessageFactory().newMessage(rawMessage, args).getFormattedMessage();
        this.logger.log(FINE, "[DEBUG] " + formattedMessage);
        getPlayersForLevel(FINE).forEach(player -> player.sendMessage(this.prefixDebug.append(text(formattedMessage, GRAY))));
    }

    /**
     * Logs an informational message formatted with the specified arguments and notifies online players who pass the informational
     * filter. The message is logged at the INFO level.
     *
     * @param rawMessage the raw message template to be formatted with the provided arguments
     * @param args       the arguments to format the raw message
     */
    public void info(String rawMessage, Object... args) {
        String formattedMessage = log.getMessageFactory().newMessage(rawMessage, args).getFormattedMessage();
        this.logger.log(INFO, formattedMessage);
        getPlayersForLevel(INFO).forEach(player -> player.sendMessage(this.prefixInfo.append(text(formattedMessage, GRAY))));
    }

    /**
     * Logs a warning message formatted with the specified arguments and notifies online players who pass the warning filter. The
     * warning message is logged at the WARNING level.
     *
     * @param rawMessage the raw warning message template to be formatted with the provided arguments
     * @param args       the arguments to format the raw warning message
     */
    public void warn(String rawMessage, Object... args) {
        String formattedMessage = log.getMessageFactory().newMessage(rawMessage, args).getFormattedMessage();
        this.logger.log(WARNING, formattedMessage);
        getPlayersForLevel(WARNING).forEach(player -> player.sendMessage(this.prefixWarn.append(text(formattedMessage, GRAY))));
    }

    /**
     * Logs an error message associated with a specific class and formats it using the provided arguments. The resulting error message
     * includes the class name and the formatted message. The error is logged using an exception containing the formatted message.
     *
     * @param errorClass the class associated with the error message, must not be null
     * @param rawMessage the raw error message template to be formatted with the provided arguments
     * @param args       the arguments to format the raw error message
     */
    public void error(@NonNull Class<?> errorClass, String rawMessage, Object... args) {
        String formattedMessage = errorClass.getName() + ": " + log.getMessageFactory().newMessage(rawMessage, args).getFormattedMessage();
        error(new Exception(formattedMessage), formattedMessage);
    }

    /**
     * Logs an error message formatted with the specified arguments and notifies online players if they pass the error filter. The
     * error message is logged at the SEVERE level and includes the stack trace of the throwable.
     *
     * @param throwable  the exception or error to log and display to players
     * @param rawMessage the raw error message template to be formatted with the provided arguments
     * @param args       the arguments to format the raw error message
     */
    public void error(Throwable throwable, String rawMessage, Object... args) {
        String formattedMessage = log.getMessageFactory().newMessage(rawMessage, args).getFormattedMessage();
        error(throwable, formattedMessage);
    }

    /**
     * Logs an error message and notifies online players if they pass the error filter. The error message is logged at the SEVERE level
     * and includes the stack trace of the throwable.
     *
     * @param throwable the exception or error to log and display to players
     * @param message   the error message to log and display to players
     */
    public void error(Throwable throwable, String message) {
        this.logger.log(SEVERE, message, throwable);
        getPlayersForLevel(SEVERE).forEach(player -> player.sendMessage(this.prefixError.append(text(message, GRAY))
                .hoverEvent(getStackTraceComponent(throwable))));
    }

    private List<? extends Player> getPlayersForLevel(Level level) {
        if (!this.plugin.isPaperPlugin()) {
            return List.of();
        }

        Collection<? extends Player> onlinePlayers = getOnlinePlayers();
        Stream<? extends Player> filteredPlayersStream = switch (level.getName()) {
            case "DEBUG" -> onlinePlayers.stream()
                    .filter(player -> this.debugFilter.test(player));
            case "INFO" -> onlinePlayers.stream()
                    .filter(player -> this.infoFilter.test(player));
            case "WARNING" -> onlinePlayers.stream()
                    .filter(player -> this.warnFilter.test(player));
            case "SEVERE" -> onlinePlayers.stream()
                    .filter(player -> this.errorFilter.test(player));
            default -> Stream.of();
        };

        return filteredPlayersStream.toList();
    }

    private @NonNull Component getStackTraceComponent(@NonNull Throwable throwable) {
        Component component = empty()
                .append(text(throwable.getMessage(), DARK_RED));

        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            component = component.appendNewline().appendSpace().appendSpace().appendSpace().appendSpace()
                    .append(text("at " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(", RED))
                    .append(text(stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber(), GRAY))
                    .append(text(")", RED));
        }

        return component;
    }
}

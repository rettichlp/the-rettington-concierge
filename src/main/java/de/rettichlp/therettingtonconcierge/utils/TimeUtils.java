package de.rettichlp.therettingtonconcierge.utils;

import com.google.inject.Inject;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static java.time.ZoneId.of;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.GERMAN;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.regex.Pattern.compile;
import static org.bukkit.Bukkit.getScheduler;

public class TimeUtils {

    public static ZoneId ZONE_ID = of("Europe/Berlin");

    private static final DateTimeFormatter DATE_FORMAT = ofPattern("dd.MM.yyyy", GERMAN);
    private static final DateTimeFormatter TIME_FORMAT = ofPattern("HH:mm:ss", GERMAN);
    private static final DateTimeFormatter DATE_TIME_FORMAT = ofPattern("dd.MM.yyyy HH:mm:ss", GERMAN);
    private static final Pattern PT_DURATION_PATTERN = compile("(\\d+)([smhd])");

    @Inject
    private static JavaPlugin plugin;

    /**
     * Retrieves the current date and time using the predefined time zone.
     *
     * @return the current date and time as a {@link ZonedDateTime} object.
     */
    @Contract(" -> new")
    public static @NonNull ZonedDateTime now() {
        return ZonedDateTime.now(ZONE_ID);
    }

    /**
     * Converts the given {@link ZonedDateTime} object to a formatted date string using the predefined date format.
     *
     * @param dateTime the {@link ZonedDateTime} object to be converted. Must not be null.
     *
     * @return a non-null formatted date string representation of the given date-time.
     */
    public static @NonNull String toDateString(ZonedDateTime dateTime) {
        return DATE_FORMAT.format(dateTime);
    }

    /**
     * Converts the current date and time to a formatted time string using the predefined time format.
     *
     * @return a non-null formatted time string representation of the current date and time.
     */
    public static @NonNull String toTimeString() {
        return toTimeString(now());
    }

    /**
     * Converts the given {@link ZonedDateTime} object to a formatted time string using the predefined time format.
     *
     * @param dateTime the {@link ZonedDateTime} object to be converted. Must not be null.
     *
     * @return a non-null formatted time string representation of the given date-time.
     */
    public static @NonNull String toTimeString(ZonedDateTime dateTime) {
        return TIME_FORMAT.format(dateTime);
    }

    /**
     * Converts the given time in milliseconds to a formatted string representation. The format used depends on the value of the
     * {@code withTimeUnit} parameter. If {@code withTimeUnit} is true, the format will include time units (e.g. "XXd XXh XXm XXs"). If
     * {@code withTimeUnit} is false, the format will follow a numerical representation (e.g. "XX:XX:XX:XX").
     *
     * @param milliseconds the time in milliseconds to be converted.
     * @param withTimeUnit whether to include time units in the formatted output.
     *
     * @return a non-null formatted string representation of the given time in milliseconds.
     */
    public static @NonNull String toTimeString(long milliseconds, boolean withTimeUnit) {
        long seconds = MILLISECONDS.toSeconds(milliseconds);
        long minutes = SECONDS.toMinutes(seconds);
        long hours = MINUTES.toHours(minutes);
        long days = HOURS.toDays(hours);

        return withTimeUnit
                ? format("%02dd %02dh %02dm %02ds", days, hours % 24, minutes % 60, seconds % 60)
                : format("%02d:%02d:%02d:%02d", days, hours % 24, minutes % 60, seconds % 60);
    }

    /**
     * Converts the given {@link ZonedDateTime} object to a formatted date-time string using the predefined date-time format.
     *
     * @param dateTime the {@link ZonedDateTime} object to be converted. Must not be null.
     *
     * @return a non-null formatted date-time string representation of the given date-time.
     */
    public static @NonNull String toDateTimeString(ZonedDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }

    /**
     * Converts a string representation of a duration into a {@link Duration} object. The input string should contain a sequence of
     * numeric values followed by time units such as 's' (seconds), 'm' (minutes), 'h' (hours), or 'd' (days).
     *
     * @param durationString the string representation of the duration. Must not be null.
     *
     * @return a {@link Duration} object represented by the input string. Returns {@link Duration#ZERO} if the input string does not
     *         match any valid pattern.
     */
    public static Duration toDuration(@NonNull String durationString) {
        Matcher matcher = PT_DURATION_PATTERN.matcher(durationString.toLowerCase().trim());
        Duration duration = ZERO;

        while (matcher.find()) {
            long value = parseLong(matcher.group(1));
            String unit = matcher.group(2);

            Duration durationToAdd = switch (unit) {
                case "s" -> ofSeconds(value);
                case "h" -> ofHours(value);
                case "d" -> ofDays(value);
                default -> ofMinutes(value);
            };

            duration = duration.plus(durationToAdd);
        }

        return duration;
    }

    /**
     * Schedules the provided {@link Runnable} to be executed asynchronously immediately. The task will be executed once and will not
     * be repeated.
     *
     * @param runnable the {@link Runnable} to be executed asynchronously. Must not be null.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runAsync(Runnable runnable) {
        return getScheduler().runTaskTimerAsynchronously(plugin, runnable, 0, -1);
    }

    /**
     * Schedules the provided {@link Runnable} to be executed synchronously immediately. The task will be executed once and will not be
     * repeated.
     *
     * @param runnable the {@link Runnable} to be executed synchronously. Must not be null.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runSync(Runnable runnable) {
        return getScheduler().runTaskTimer(plugin, runnable, 0, -1);
    }

    /**
     * Schedules the provided {@link Runnable} to be executed asynchronously after a specified delay. The task will be executed once
     * and will not be repeated.
     *
     * @param runnable the {@link Runnable} to be executed asynchronously. Must not be null.
     * @param delay    the delay, in ticks, before the task is first executed. Must be non-negative.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runAsyncLater(Runnable runnable, long delay) {
        return getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, -1);
    }

    /**
     * Schedules the provided {@link Runnable} to be executed synchronously after a specified delay. The task will be executed once and
     * will not be repeated.
     *
     * @param runnable the {@link Runnable} to be executed synchronously. Must not be null.
     * @param delay    the delay, in ticks, before the task is first executed. Must be non-negative.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runSyncLater(Runnable runnable, long delay) {
        return getScheduler().runTaskTimer(plugin, runnable, delay, -1);
    }

    /**
     * Schedules the provided {@link Runnable} to be executed asynchronously at a fixed rate. The task will first execute after the
     * specified delay and then continue to execute periodically with the given interval.
     *
     * @param runnable the {@link Runnable} to be executed asynchronously. Must not be null.
     * @param delay    the delay, in ticks, before the task is first executed. Must be non-negative.
     * @param period   the interval, in ticks, between later executions of the task. Must be non-negative.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runAsyncRepeating(Runnable runnable, long delay, long period) {
        return getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    /**
     * Schedules the provided {@link Runnable} to be executed synchronously at a fixed rate. The task will first execute after the
     * specified delay and then continue to execute periodically with the given interval.
     *
     * @param runnable the {@link Runnable} to be executed synchronously. Must not be null.
     * @param delay    the delay, in ticks, before the task is first executed. Must be non-negative.
     * @param period   the interval, in ticks, between later executions of the task. Must be non-negative.
     *
     * @return the {@link BukkitTask} representing the scheduled task.
     */
    public static @NonNull BukkitTask runSyncRepeating(Runnable runnable, long delay, long period) {
        return getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }
}

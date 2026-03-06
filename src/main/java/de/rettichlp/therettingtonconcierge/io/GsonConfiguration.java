package de.rettichlp.therettingtonconcierge.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import de.rettichlp.therettingtonconcierge.io.api.serialization.ExclusionDeserializationStrategy;
import de.rettichlp.therettingtonconcierge.io.api.serialization.ExclusionSerializationStrategy;
import de.rettichlp.therettingtonconcierge.io.configuration.ConfigurationSerializableTypeHierarchyAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class GsonConfiguration {

    /**
     * Represents the Gson instance used for JSON serialization and deserialization.
     */
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .addSerializationExclusionStrategy(new ExclusionSerializationStrategy())
            .addDeserializationExclusionStrategy(new ExclusionDeserializationStrategy())
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableTypeHierarchyAdapter())
            .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, _, _) -> Instant.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (instant, _, _) -> new JsonPrimitive(instant.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, _, _) -> LocalDate.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (date, _, _) -> new JsonPrimitive(date.format(ISO_ZONED_DATE_TIME)))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, _, _) -> LocalTime.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (date, _, _) -> new JsonPrimitive(date.format(ISO_ZONED_DATE_TIME)))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, _, _) -> LocalDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (date, _, _) -> new JsonPrimitive(date.format(ISO_ZONED_DATE_TIME)))
            .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, _, _) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(ZonedDateTime.class, (JsonSerializer<ZonedDateTime>) (date, _, _) -> new JsonPrimitive(date.format(ISO_ZONED_DATE_TIME)))
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>) (json, _, _) -> Duration.parse(json.getAsJsonPrimitive().getAsString()))
            .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>) (duration, _, _) -> new JsonPrimitive(duration.toString()))
            .create();
}

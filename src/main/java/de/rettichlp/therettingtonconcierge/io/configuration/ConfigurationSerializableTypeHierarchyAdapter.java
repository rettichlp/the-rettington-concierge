package de.rettichlp.therettingtonconcierge.io.configuration;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import lombok.NoArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;

@NoArgsConstructor
public final class ConfigurationSerializableTypeHierarchyAdapter implements JsonSerializer<ConfigurationSerializable>,
                                                                            JsonDeserializer<ConfigurationSerializable> {

    static final TypeToken<Map<String, Object>> MAP_TYPE = new TypeToken<Map<String, Object>>() {
    };

    private static final String SERIALIZED_TYPE_KEY = "==";

    @Override
    public JsonElement serialize(ConfigurationSerializable configurationSerializable,
                                 Type type,
                                 @NonNull JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(serializeToMap(configurationSerializable), MAP_TYPE.getType());
    }

    @Override
    public ConfigurationSerializable deserialize(JsonElement jsonElement,
                                                 Type type,
                                                 @NonNull JsonDeserializationContext jsonDeserializationContext) throws
                                                                                                                 JsonParseException {
        return deserializeFromMap(jsonDeserializationContext.deserialize(jsonElement, MAP_TYPE.getType()));
    }

    private ConfigurationSerializable deserializeFromMap(Map<String, Object> map) throws IllegalArgumentException {
        deserializeInner(map);
        return ConfigurationSerialization.deserializeObject(map);
    }

    private @NonNull Map<String, Object> serializeToMap(@NonNull ConfigurationSerializable serializable) {
        Map<String, Object> map = new HashMap<>(serializable.serialize());
        map.put(SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
        serializeInner(map);
        return map;
    }

    private void serializeInner(@NonNull Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof ConfigurationSerializable configurationSerializable) {
                Map<String, Object> innerMap = serializeToMap(configurationSerializable);
                entry.setValue(innerMap);
            }
        }
    }

    private void deserializeInner(@NonNull Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object raw = entry.getValue();
            if (raw instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked") // We know that the value is a map of strings to objects
                Map<String, Object> innerMap = (Map<String, Object>) raw;
                deserializeInner(innerMap);

                if (innerMap.containsKey(SERIALIZED_TYPE_KEY)) {
                    String alias = (String) innerMap.get(SERIALIZED_TYPE_KEY);
                    Class<? extends ConfigurationSerializable> clazz = ConfigurationSerialization.getClassByAlias(alias);
                    if (clazz == null) {
                        throw new IllegalArgumentException("Could not find class by alias: " + alias);
                    }

                    ConfigurationSerializable serializable = ConfigurationSerialization.deserializeObject(innerMap, clazz);
                    entry.setValue(serializable);
                }
            } else if (raw instanceof Number number) {
                entry.setValue(narrowNumberType(number));
            }
        }
    }

    private Number narrowNumberType(@NonNull Number number) {
        long asLong = number.longValue();
        if (number.doubleValue() == (double) asLong) {
            return asLong <= MAX_VALUE && asLong >= MIN_VALUE ? number.intValue() : asLong;
        } else {
            return number;
        }
    }
}

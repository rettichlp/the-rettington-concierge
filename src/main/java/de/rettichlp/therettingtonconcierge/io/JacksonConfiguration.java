package de.rettichlp.therettingtonconcierge.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonConfiguration {

    /**
     * Represents the ObjectMapper instance used for JSON serialization and deserialization.
     */
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());
}

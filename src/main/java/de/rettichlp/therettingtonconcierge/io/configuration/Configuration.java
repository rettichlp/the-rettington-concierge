package de.rettichlp.therettingtonconcierge.io.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface Configuration {

    /**
     * Returns the name of the configuration file as specified in the annotation.
     *
     * @return the name of the configuration file, without its extension
     */
    String fileName();

    /**
     * Retrieves the file type associated with the configuration. The file type determines the format (e.g., JSON, YAML) of the
     * configuration.
     *
     * @return the file type of the configuration as a {@link ConfigurationType} enumeration value
     */
    ConfigurationType fileType();

    /**
     * An enumeration representing supported configuration file types. Each type associates a specific file extension used for
     * serialization and deserialization of configuration data.
     */
    @Getter
    @AllArgsConstructor
    enum ConfigurationType {

        /**
         * Represents the JSON configuration file type with the associated file extension.
         */
        JSON(".json"),
        /**
         * Represents the YAML configuration type with the file extension ".yaml".
         */
        YAML(".yaml");

        private final String extension;
    }
}

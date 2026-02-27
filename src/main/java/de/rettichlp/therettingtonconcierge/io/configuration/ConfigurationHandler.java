package de.rettichlp.therettingtonconcierge.io.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature.WRITE_DOC_START_MARKER;
import static com.google.common.base.Preconditions.checkArgument;
import static de.rettichlp.therettingtonconcierge.io.GsonConfiguration.GSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

@Log4j2
public class ConfigurationHandler {

    /**
     * Represents the ObjectMapper instance used for YAML serialization and deserialization.
     */
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory().disable(WRITE_DOC_START_MARKER))
            .findAndRegisterModules()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .disable(WRITE_DATES_AS_TIMESTAMPS);

    private final String namespace;
    private final java.io.File pluginFolder;

    public ConfigurationHandler(String namespace) {
        this.namespace = namespace;
        this.pluginFolder = new java.io.File("plugins/" + this.namespace);
    }

    /**
     * Loads a configuration object of the specified class type. The method uses a {@link ConfigurationHolder} to read and deserialize
     * configuration data from a file based on the {@link Configuration} annotation present on the class. If the configuration file
     * does not exist, it will be automatically created along with a default configuration.
     *
     * @param <T>                the type of the configuration object, which must be annotated with {@link Configuration}
     * @param configurationClass the class of the configuration object to load, must not be null and must be annotated with
     *                           {@link Configuration}
     *
     * @return the deserialized configuration object of the specified type
     *
     * @throws IllegalStateException if the configuration class is not annotated with {@link Configuration} or if deserialization of
     *                               the configuration file fails
     */
    public <T> T loadConfiguration(Class<T> configurationClass) {
        ConfigurationHolder<T> configurationHolder = getConfigurationData(configurationClass);
        configurationHolder.deserialize();
        return configurationHolder.getConfiguration();
    }

    /**
     * Saves the provided configuration object by serializing its data to the corresponding configuration file. The method fetches a
     * {@link ConfigurationHolder} instance for the configuration class, sets the provided configuration object, and invokes the
     * serialization process.
     *
     * @param <T>           the type of the configuration object, which must be annotated with {@link Configuration}
     * @param configuration the configuration object to be saved, must not be null
     *
     * @throws IllegalStateException if the configuration class is not annotated with {@link Configuration} or if serialization to the
     *                               configuration file fails
     */
    @SuppressWarnings("unchecked")
    public <T> void saveConfiguration(@NonNull T configuration) {
        ConfigurationHolder<T> configurationHolder = getConfigurationData((Class<T>) configuration.getClass());
        configurationHolder.setConfiguration(configuration);
        configurationHolder.serialize();
    }

    private <T> @NonNull ConfigurationHolder<T> getConfigurationData(@NonNull Class<T> configurationClass) {
        if (!configurationClass.isAnnotationPresent(Configuration.class)) {
            throw new IllegalStateException("Class is not annotated with @Configuration");
        }

        Configuration configuration = configurationClass.getAnnotation(Configuration.class);

        createPluginDirectory();
        File configurationFile = new File(this.pluginFolder, configuration.fileName() + configuration.fileType().getExtension());

        ConfigurationHolder<T> configurationHolder = new ConfigurationHolder<>(configurationFile, configuration, configurationClass, null);

        if (!configurationFile.exists()) {
            configurationHolder.createConfigurationFile();
        }

        return configurationHolder;
    }

    /**
     * Creates the plugin directory if it does not already exist. Logs a message if the directory is successfully created.
     */
    private void createPluginDirectory() {
        if (this.pluginFolder.mkdirs()) {
            log.info("Plugin folder created successfully");
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ConfigurationHolder<T> {

        private final File file;
        private final Configuration annotation;
        private final Class<T> configurationClass;

        @Setter
        private T configuration;

        public void createConfigurationFile() {
            try {
                if (this.file.createNewFile()) {
                    log.info("Configuration file created successfully: {}", this.file.getName());
                }

                this.configuration = createDefaultConfiguration();

                serialize();
            } catch (ReflectiveOperationException | IOException e) {
                throw new IllegalStateException("Failed to create config file: " + e.getMessage());
            }
        }

        public void deserialize() {
            try (FileReader fileReader = new FileReader(this.file)) {
                this.configuration = switch (this.annotation.fileType()) {
                    case JSON -> GSON.fromJson(fileReader, this.configurationClass);
                    case YAML -> YAML.readValue(fileReader, this.configurationClass);
                };
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read config file: " + e.getMessage());
            }
        }

        public void serialize() {
            checkArgument(nonNull(this.configuration), "Configuration object is null");

            try (FileWriter fileWriter = new FileWriter(this.file, UTF_8)) {
                String content = switch (this.annotation.fileType()) {
                    case JSON -> GSON.toJson(this.configuration);
                    case YAML -> YAML.writeValueAsString(this.configuration);
                };

                fileWriter.write(content);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to save config file: " + e.getMessage());
            }
        }

        private @NonNull T createDefaultConfiguration() throws NoSuchMethodException, InvocationTargetException,
                                                               InstantiationException, IllegalAccessException {
            return this.configurationClass.getDeclaredConstructor().newInstance();
        }
    }
}

package de.rettichlp.therettingtonconcierge.translation;

import com.google.inject.Inject;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import de.rettichlp.therettingtonconcierge.utils.TextUtils;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.TranslationStore;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.ResourceBundle.getBundle;
import static net.kyori.adventure.key.Key.key;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.tag.Tag.inserting;
import static net.kyori.adventure.translation.GlobalTranslator.translator;
import static net.kyori.adventure.translation.TranslationStore.messageFormat;

@Getter
public class I18nMiniMessageTranslator extends MiniMessageTranslator {

    private static final Collection<I18nMiniMessageTranslator> INSTANCES = new CopyOnWriteArrayList<>();

    private final JavaPlugin plugin;
    private final LogDispatcher logDispatcher;
    private final TranslationStore.@NonNull StringBased<MessageFormat> translationStore;

    @Inject
    public I18nMiniMessageTranslator(@NonNull JavaPlugin plugin, LogDispatcher logDispatcher) {
        this.plugin = plugin;
        this.logDispatcher = logDispatcher;
        this.translationStore = messageFormat(key(this.plugin.namespace() + ":i18n"));

        // add this translation instance to instance holder for multi-plugin-support
        INSTANCES.add(this);
        // add this translator to the GlobalTranslator
        translator().addSource(this);
    }

    @Override
    public @NotNull Key name() {
        return this.translationStore.name();
    }

    public void loadTranslations(Locale defaultLocale, Locale @NonNull ... additionalLocales) {
        this.translationStore.defaultLocale(defaultLocale);

        // load translation files
        loadTranslationFile(defaultLocale);
        for (Locale supportedLocale : additionalLocales) {
            loadTranslationFile(supportedLocale);
        }
    }

    @Override
    protected @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        return ofNullable(this.translationStore.translate(key, locale))
                .map(MessageFormat::toPattern)
                .orElse(null); // Returning null "ignores" this translation.
    }

    private void loadTranslationFile(Locale locale) {
        ResourceBundle bundle = getBundle("lang/" + locale.getLanguage(), locale, this.plugin.getClass().getClassLoader());
        this.translationStore.registerAll(locale, bundle, false);
        this.logDispatcher.debug(getClass(), "Registered bundle {} for locale {}", this.plugin.namespace(), locale);
    }

    public static @NonNull Component localize(@NonNull String translationKey, @NonNull Locale locale, Object @NonNull ... args) {
        // map arguments to components
        List<@NotNull Component> argComponents = stream(args).map(TextUtils::objectToComponent).toList();

        // raw translation from translation stores
        Optional<String> optionalRawTranslation = getRawTranslation(translationKey, locale);
        if (optionalRawTranslation.isEmpty()) {
            return text(translationKey); // fallback
        }

        String rawTranslation = optionalRawTranslation.get(); // something like "<red>Hello <arg:0>!</red>"

        // create the custom mini-message builder with <arg> and <br> tag
        MiniMessage builder = getMiniMessageBuilder(argComponents);

        // deserialize mini-message to component
        return builder.deserialize(rawTranslation);
    }

    public static @NonNull List<Component> localizeMultiline(@NonNull String translationKey,
                                                             @NonNull Locale locale,
                                                             Object @NonNull ... args) {
        // map arguments to components
        List<@NotNull Component> argComponents = stream(args).map(TextUtils::objectToComponent).toList();

        // raw translation from translation stores
        Optional<String> optionalRawTranslation = getRawTranslation(translationKey, locale);
        if (optionalRawTranslation.isEmpty()) {
            return singletonList(text(translationKey)); // fallback
        }

        String rawTranslation = optionalRawTranslation.get(); // something like "<red>Hello <arg:0>!</red>"

        // create the custom mini-message builder with <arg> and <br> tag
        MiniMessage builder = getMiniMessageBuilder(argComponents);

        // deserialize mini-message to components
        return stream(rawTranslation.split("\n")).map(builder::deserialize).toList();
    }

    private static Optional<String> getRawTranslation(@NonNull String translationKey, @NonNull Locale locale) {
        return INSTANCES.stream()
                .filter(translator -> translator.getTranslationStore().canTranslate(translationKey, locale))
                .findFirst()
                .map(translator -> translator.getTranslationStore().translate(translationKey, locale))
                .map(MessageFormat::toPattern);
    }

    @Contract("_ -> new")
    private static @NonNull MiniMessage getMiniMessageBuilder(List<Component> argComponents) {
        return MiniMessage.builder()
                .editTags(b -> b.tag("arg", (argumentQueue, _) -> {
                    String value = argumentQueue.popOr("The arg tag requires exactly one argument").value();
                    Component component = argComponents.get(parseInt(value));
                    return inserting(component);
                }))
                .build();
    }
}

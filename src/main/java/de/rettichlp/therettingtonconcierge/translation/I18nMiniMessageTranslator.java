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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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

    private final JavaPlugin plugin;
    private final LogDispatcher logDispatcher;
    private final String namespace;
    private final TranslationStore.@NonNull StringBased<MessageFormat> translationStore;

    @Inject
    public I18nMiniMessageTranslator(@NonNull JavaPlugin plugin, LogDispatcher logDispatcher) {
        this.plugin = plugin;
        this.logDispatcher = logDispatcher;
        this.namespace = plugin.namespace();
        this.translationStore = messageFormat(key(this.namespace + ":i18n"));

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

    public @NonNull Component localize(@NonNull String translationKey, @NonNull Locale locale, Object @NonNull ... args) {
        // map arguments to components
        List<@NotNull Component> argComponents = stream(args).map(TextUtils::objectToComponent).toList();

        // raw string from translation store
        String rawString = getMiniMessageString(translationKey, locale); // something like "<red>Hello <arg:0>!</red>"

        if (rawString == null) {
            this.logDispatcher.warn("Missing translation for key {} in locale {}", translationKey, locale);
            return text(translationKey); // fallback
        }

        // create the custom mini-message builder with <arg> and <br> tag
        MiniMessage builder = MiniMessage.builder()
                .editTags(b -> b.tag("arg", (argumentQueue, _) -> {
                    String value = argumentQueue.popOr("The arg tag requires exactly one argument").value();
                    Component component = argComponents.get(parseInt(value));
                    return inserting(component);
                }))
                .build();

        // deserialize mini-message to component
        return builder.deserialize(rawString);
    }

    public @NonNull List<Component> localizeMultiline(@NonNull String translationKey,
                                                      @NonNull Locale locale,
                                                      Object @NonNull ... args) {
        // map arguments to components
        List<@NotNull Component> argComponents = stream(args).map(TextUtils::objectToComponent).toList();

        // raw string from translation store
        String rawString = getMiniMessageString(translationKey, locale); // something like "<red>Hello <arg:0>!</red>"

        if (rawString == null) {
            this.logDispatcher.warn("Missing translation for key {} in locale {}", translationKey, locale);
            return singletonList(text(translationKey)); // fallback
        }

        // create the custom mini-message builder with <arg> and <br> tag
        MiniMessage builder = MiniMessage.builder()
                .editTags(b -> b.tag("arg", (argumentQueue, _) -> {
                    String value = argumentQueue.popOr("The arg tag requires exactly one argument").value();
                    Component component = argComponents.get(parseInt(value));
                    return inserting(component);
                }))
                .build();

        // deserialize mini-message to components
        return stream(rawString.split("\n")).map(builder::deserialize).toList();
    }

    @Override
    protected @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        return ofNullable(this.translationStore.translate(key, locale))
                .map(MessageFormat::toPattern)
                .orElse(null); // Returning null "ignores" this translation.
    }

    private void loadTranslationFile(Locale locale) {
        ResourceBundle bundle = getBundle(this.namespace, locale, UTF8ResourceBundleControl.get());
        this.translationStore.registerAll(locale, bundle, false);
        this.logDispatcher.debug(getClass(), "Registered bundle {} for locale {}", this.namespace, locale);
    }
}

package de.rettichlp.therettingtonconcierge.translation;

import com.google.inject.Inject;
import de.rettichlp.therettingtonconcierge.logging.LogDispatcher;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static java.util.Optional.ofNullable;
import static java.util.ResourceBundle.getBundle;
import static net.kyori.adventure.key.Key.key;
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

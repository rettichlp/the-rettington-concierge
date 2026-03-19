package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

import static io.papermc.paper.registry.data.dialog.action.DialogAction.customClick;
import static io.papermc.paper.registry.data.dialog.type.DialogType.confirmation;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.translation.GlobalTranslator.render;

public abstract class AbstractInputDialog<T> extends AbstractDialog {

    public AbstractInputDialog(Player player, Component titleComponent) {
        super(player, titleComponent);
    }

    public abstract T extractor(@NonNull DialogResponseView dialogResponseView);

    public abstract void inputHandler(T value);

    @Override
    public DialogType type() {
        Locale locale = this.player.locale();

        return confirmation(
                ActionButton.builder(render(translatable("gui.back"), locale)).build(),
                ActionButton.builder(render(translatable("gui.ok"), locale))
                        .action(customClick((dialogResponseView, _) -> {
                            T extractedValue = extractor(dialogResponseView);
                            inputHandler(extractedValue);
                        }, ClickCallback.Options.builder().build()))
                        .build()
        );
    }
}

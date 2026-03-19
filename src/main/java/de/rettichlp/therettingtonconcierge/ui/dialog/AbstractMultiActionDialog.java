package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

import static io.papermc.paper.registry.data.dialog.ActionButton.create;
import static io.papermc.paper.registry.data.dialog.type.DialogType.multiAction;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;

public abstract class AbstractMultiActionDialog extends AbstractDialog {

    public AbstractMultiActionDialog(Player player, Component titleComponent) {
        super(player, titleComponent);
    }

    public abstract int columnCount();

    public abstract List<ActionButton> actions();

    @Override
    public List<? extends DialogBody> body() {
        return List.of();
    }

    @Override
    public DialogType type() {
        return multiAction(actions(), exitButton(), columnCount());
    }

    public ActionButton exitButton() {
        return create(translatable("gui.back"), empty(), 250, null);
    }
}

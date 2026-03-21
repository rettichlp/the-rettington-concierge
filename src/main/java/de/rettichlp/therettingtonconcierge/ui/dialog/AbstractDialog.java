package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.AllArgsConstructor;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static io.papermc.paper.dialog.Dialog.create;
import static net.kyori.adventure.translation.GlobalTranslator.render;

@AllArgsConstructor
public abstract class AbstractDialog {

    protected final Player player;

    private final Component titleComponent;

    public abstract List<? extends DialogBody> body();

    public abstract DialogType type();

    public void open() {
        open(this.player);
    }

    public void open(@NonNull Audience player) {
        Dialog dialog = create(builder -> builder.empty()
                .base(getDialogBase())
                .type(type()));

        player.showDialog(dialog);
    }

    public List<? extends DialogInput> inputs() {
        return List.of();
    }

    @Contract(" -> new")
    private @NonNull DialogBase getDialogBase() {
        return DialogBase.builder(render(this.titleComponent, this.player.locale()))
                .canCloseWithEscape(true)
                .body(body())
                .inputs(inputs())
                .build();
    }
}

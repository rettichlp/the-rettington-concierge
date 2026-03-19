package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

import static io.papermc.paper.registry.data.dialog.input.DialogInput.bool;
import static java.util.Collections.singletonList;

@Getter
@Setter
public class BooleanInputDialog extends AbstractInputDialog<Boolean> {

    private final Component label;
    private final Consumer<Boolean> inputHandler;

    private boolean initial = false;
    private String onTrue = "true";
    private String onFalse = "false";

    public BooleanInputDialog(Player player, Component title, Component label, @NonNull Consumer<Boolean> inputHandler) {
        super(player, title);
        this.label = label;
        this.inputHandler = inputHandler;
    }

    @Override
    public Boolean extractor(@NonNull DialogResponseView dialogResponseView) {
        return dialogResponseView.getBoolean("input");
    }

    @Override
    public void inputHandler(Boolean value) {
        this.inputHandler.accept(value);
    }

    @Override
    public List<? extends DialogBody> body() {
        return List.of();
    }

    @Override
    public List<? extends DialogInput> inputs() {
        return singletonList(bool("input", this.label, this.initial, this.onTrue, this.onFalse));
    }
}

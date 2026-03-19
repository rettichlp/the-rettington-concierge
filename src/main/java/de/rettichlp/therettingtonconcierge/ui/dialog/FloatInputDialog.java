package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static io.papermc.paper.registry.data.dialog.input.DialogInput.numberRange;
import static java.util.Collections.singletonList;

@Getter
@Setter
public class FloatInputDialog extends AbstractInputDialog<Float> {

    private final Component label;
    private final Consumer<Float> inputHandler;

    private int width = 200;
    private String labelFormat = "options.generic_value";
    private float start;
    private float end;
    private @Nullable Float initial = null;
    private @Nullable Float step = null;

    public FloatInputDialog(Player player, Component title, Component label, @NonNull Consumer<Float> inputHandler) {
        super(player, title);
        this.label = label;
        this.inputHandler = inputHandler;
    }

    @Override
    public Float extractor(@NonNull DialogResponseView dialogResponseView) {
        return dialogResponseView.getFloat("input");
    }

    @Override
    public void inputHandler(Float value) {
        this.inputHandler.accept(value);
    }

    @Override
    public List<? extends DialogBody> body() {
        return List.of();
    }

    @Override
    public List<? extends DialogInput> inputs() {
        return singletonList(numberRange("input", this.width, this.label, this.labelFormat, this.start, this.end, this.initial, this.step));
    }
}

package de.rettichlp.therettingtonconcierge.ui.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static io.papermc.paper.registry.data.dialog.input.DialogInput.text;
import static java.util.Collections.singletonList;

@Getter
@Setter
public class TextInputDialog extends AbstractInputDialog<String> {

    private final Component label;
    private final Consumer<String> inputHandler;

    private @Range(from = 1L, to = 1024L) int width = 200;
    private boolean labelVisible = true;
    private String initial = "";
    private @Positive int maxLength = 32;
    private TextDialogInput.@Nullable MultilineOptions multilineOptions = null;

    public TextInputDialog(Player player, Component title, Component label, @NonNull Consumer<String> inputHandler) {
        super(player, title);
        this.label = label;
        this.inputHandler = inputHandler;
    }

    @Override
    public String extractor(@NonNull DialogResponseView dialogResponseView) {
        return dialogResponseView.getText("input");
    }

    @Override
    public void inputHandler(String value) {
        this.inputHandler.accept(value);
    }

    @Override
    public List<? extends DialogBody> body() {
        return List.of();
    }

    @Override
    public List<? extends DialogInput> inputs() {
        return singletonList(text("input", this.width, this.label, this.labelVisible, this.initial, this.maxLength, this.multilineOptions));
    }
}

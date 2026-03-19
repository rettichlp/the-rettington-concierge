package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ISearchable<E> {

    Component searchItemName();

    List<Component> searchItemTooltip(@NonNull String searchString);

    boolean searchFunction(@NonNull E element, @NonNull String searchString);
}

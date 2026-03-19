package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ISearchable<E> {

    Component searchItemTitle();

    List<Component> searchItemTooltip(@NonNull String currentSearchString);

    boolean searchFunction(@NonNull E element, @NonNull String searchFilter);
}

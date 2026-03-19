package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public interface ISortable<E> {

    Component sortItemTitle();

    List<Component> sortItemTooltip(@NonNull String sortString);

    Map<String, Comparator<E>> comparators();
}

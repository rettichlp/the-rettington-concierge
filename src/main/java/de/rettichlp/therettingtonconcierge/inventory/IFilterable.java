package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface IFilterable<E> {

    Component filterItemName();

    List<Component> filterItemTooltip(@NonNull String filterString);

    Map<String, Predicate<E>> filters();
}

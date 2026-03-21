package de.rettichlp.therettingtonconcierge.inventory;

import java.util.Map;
import java.util.function.Predicate;

public interface IFilterable<E> {

    Map<String, Predicate<E>> filters();
}

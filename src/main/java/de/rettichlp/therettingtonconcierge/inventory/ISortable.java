package de.rettichlp.therettingtonconcierge.inventory;

import java.util.Comparator;
import java.util.Map;

public interface ISortable<E> {

    Map<String, Comparator<E>> comparators();
}

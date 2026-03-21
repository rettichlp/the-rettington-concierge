package de.rettichlp.therettingtonconcierge.inventory;

import org.jspecify.annotations.NonNull;

public interface ISearchable<E> {

    boolean searchFunction(@NonNull E element, @NonNull String searchString);
}

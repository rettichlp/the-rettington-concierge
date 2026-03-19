package de.rettichlp.therettingtonconcierge.inventory;

public interface ISearchable<E> {

    boolean searchFunction(E e, String searchFilter);
}

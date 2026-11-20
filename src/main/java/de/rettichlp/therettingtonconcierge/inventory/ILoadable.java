package de.rettichlp.therettingtonconcierge.inventory;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public interface ILoadable<T> {

    /**
     * Loads the data of the menu. Called when the menu is opened. It may run on any thread.
     *
     * @return the data
     */
    @NonNull CompletableFuture<T> load();
}

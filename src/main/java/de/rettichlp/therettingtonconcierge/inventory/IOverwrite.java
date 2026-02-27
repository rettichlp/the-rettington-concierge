package de.rettichlp.therettingtonconcierge.inventory;

import org.jspecify.annotations.NonNull;

public interface IOverwrite {

    /**
     * Overwrites the state of the provided {@link RegisteredInventory.Builder}.
     *
     * @param registeredInventoryBuilder the builder instance of RegisteredInventory to be modified, must not be null
     */
    void overwrite(RegisteredInventory.@NonNull Builder registeredInventoryBuilder);
}

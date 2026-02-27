package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public abstract class GameMenu {

    /**
     * Retrieves the title component of the game menu, which is used as the display title when opening the inventory.
     *
     * @return the title component of the game menu
     */
    public abstract Component title();

    /**
     * Retrieves the pattern used for configuring the registered inventory in the game menu.
     *
     * @return the pattern instance used for structuring and organizing the registered inventory
     */
    public abstract int size();

    /**
     * Constructs and configures the inventory for the game menu based on the specified player, registered inventory builder, and page
     * number. This method allows customization of the inventory's contents and behavior.
     *
     * @param player                     the player for whom the inventory is being built
     * @param registeredInventoryBuilder the builder instance for configuring the registered inventory
     * @param page                       the current page number to be displayed in the inventory
     * @param tabIndex                   the index of the tab being built, if applicable
     */
    public abstract void builder(Player player,
                                 RegisteredInventory.@NonNull Builder registeredInventoryBuilder,
                                 int page,
                                 int tabIndex);

    /**
     * Opens the game menu for the specified player, constructing and configuring the inventory using provided title, pattern, and
     * optional behaviors.
     *
     * @param player the player for whom the game menu is opened
     */
    public void open(Player player) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        if (this instanceof IOpenSound iOpenSound) {
            registeredInventoryBuilder.openSound(iOpenSound.openSound());
        }

        builder(player, registeredInventoryBuilder, 1, 0);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(player);
    }
}

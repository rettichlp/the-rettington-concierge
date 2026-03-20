package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public abstract class GameMenu {

    protected final Player player;

    public GameMenu(Player player) {
        this.player = player;
    }

    /**
     * Retrieves the title of the game menu. The title is typically used as the display name of the menu when it is opened for a
     * player.
     *
     * @return a {@link Component} object representing the title of the game menu
     */
    public abstract Component title();

    /**
     * Retrieves the size of the game menu. The size determines the number of inventory slots available in the menu.
     *
     * @return an integer representing the size of the game menu
     */
    public abstract int size();

    /**
     * Configures and builds the registered inventory menu for the player, based on the provided arguments.
     *
     * @param registeredInventoryBuilder the builder instance used to create the registered inventory menu
     * @param page                       the current page number of the menu being built
     * @param tabIndex                   the index of the current active tab in the menu
     */
    public abstract void builder(RegisteredInventory.@NonNull Builder registeredInventoryBuilder, int page, int tabIndex);

    /**
     * Opens the game menu for the associated player. This method initializes a {@link RegisteredInventory.Builder} with the menu's
     * title and size, and further configures it to include additional features based on the context of the {@code GameMenu} instance.
     */
    public void open() {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        if (this instanceof IOpenSound iOpenSound) {
            registeredInventoryBuilder.openSound(iOpenSound.openSound());
        }

        builder(registeredInventoryBuilder, 1, 0);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(this.player);
    }
}

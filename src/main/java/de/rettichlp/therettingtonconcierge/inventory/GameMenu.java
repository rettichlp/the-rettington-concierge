package de.rettichlp.therettingtonconcierge.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public abstract class GameMenu {

    protected final Player player;

    private RegisteredInventory registeredInventory;

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
     * Returns whether the player has this menu open right now.
     */
    public boolean isOpen() {
        return this.registeredInventory != null && this.registeredInventory.isOpen();
    }

    /**
     * Opens the game menu for the associated player. This method initializes a {@link RegisteredInventory.Builder} with the menu's
     * title and size, and further configures it to include additional features based on the context of the {@code GameMenu} instance.
     */
    public void open() {
        openAt(1);
    }

    /**
     * Rebuilds the content of the menu and shows it in the inventory that is open already, without opening a new inventory. Use it
     * when the content of the menu changed, e.g. after loading data. It does nothing if the player closed the menu or opened another
     * one, so it never opens something by itself.
     *
     * @return {@code true} if the content of the open menu was updated, {@code false} if the menu is not open
     */
    public boolean refresh() {
        return refreshAt(1);
    }

    /**
     * Opens the game menu with the given page passed to {@link #builder(RegisteredInventory.Builder, int, int)}. A menu with pages
     * offers it as its own method.
     *
     * @param page the page to build the menu for
     */
    protected void openAt(int page) {
        this.registeredInventory = createBuilder(page).openInventory(this.player);
    }

    /**
     * Same as {@link #refresh()}, but with the given page passed to {@link #builder(RegisteredInventory.Builder, int, int)}. A menu
     * with pages offers it as its own method.
     *
     * @param page the page to build the menu for
     *
     * @return {@code true} if the content of the open menu was updated, {@code false} if the menu is not open
     */
    protected boolean refreshAt(int page) {
        if (!isOpen()) {
            return false;
        }

        createBuilder(page).updateInventory(this.registeredInventory);
        return true;
    }

    private RegisteredInventory.Builder createBuilder(int page) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        if (this instanceof IOpenSound iOpenSound) {
            registeredInventoryBuilder.openSound(iOpenSound.openSound());
        }

        builder(registeredInventoryBuilder, page, 0);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        return registeredInventoryBuilder;
    }
}

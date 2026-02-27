package de.rettichlp.therettingtonconcierge.inventory;

import de.rettichlp.therettingtonconcierge.inventory.item.Item;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static java.lang.Math.min;
import static java.util.Arrays.copyOfRange;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static org.bukkit.Material.PAPER;

@Getter
public abstract class PaginatedGameMenu<E> extends GameMenu {

    private int currentPage = 0;

    /**
     * Retrieves an array of elements to be displayed in the game menu for the specified player and page. This method typically returns
     * the elements to be used for constructing the items shown on the menu within the current context of the game.
     *
     * @param player the player for whom the elements are being retrieved
     *
     * @return an array of elements to be displayed in the menu for the given player
     */
    public abstract E[] elements(Player player);

    /**
     * Creates an {@link ItemStack} representation of the specified element for the given player. This method is primarily used to
     * construct and display items within a game menu for game interactions.
     *
     * @param player  the player for whom the item stack is being created
     * @param element the element to be represented as an item stack, must not be null
     *
     * @return the {@link ItemStack} corresponding to the given element for the specified player
     */
    public abstract ItemStack itemStack(Player player, @NonNull E element);

    /**
     * Handles the interaction logic when a player clicks on an element in the game menu. This method is triggered by different click
     * types performed by the player on specific elements of the menu.
     *
     * @param player    the player interacting with the menu
     * @param clickType the type of click action performed by the player
     * @param element   the specific element clicked within the menu, must not be null
     */
    public abstract void clickFunction(Player player, ClickType clickType, @NonNull E element);

    @Override
    public void builder(Player player, RegisteredInventory.@NonNull Builder registeredInventoryBuilder, int page, int tabIndex) {
        this.currentPage = page;

        // add elements for the current page
        for (E element : getPageElements(player)) {
            registeredInventoryBuilder
                    .add(itemStack(player, element), (clicker, itemStacks, clickType, inventorySlots) -> clickFunction(clicker, clickType, element));
        }

        addPageControl(registeredInventoryBuilder, player);
    }

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    /**
     * Opens the game menu for the specified player at the given page. This method builds the inventory based on a specified page,
     * allows for overwriting inventory configurations if applicable, and then opens the inventory for the player.
     *
     * @param player the player for whom the menu is to be opened
     * @param page   the page number of the menu to display
     */
    public void open(Player player, int page) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        builder(player, registeredInventoryBuilder, page, 0);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(player);
    }

    private @NonNull @Unmodifiable List<E> getPageElements(Player player) {
        int pageSize = size() - 9; // the last row is reserved for page control
        E[] elements = elements(player);
        E[] pageElements = copyOfRange(elements, (this.currentPage - 1) * pageSize, min(elements.length, this.currentPage * pageSize));
        return List.of(pageElements);
    }

    private void addPageControl(RegisteredInventory.Builder registeredInventoryBuilder, Player player) {
        if (this.currentPage > 1) {
            registeredInventoryBuilder
                    .item(-6, Item.builder(PAPER)
                            .displayName(text("«", DARK_GRAY))
                            .build(), (clicker, _, _, _) -> open(clicker, this.currentPage - 1));
        }

        int pageSize = size() - 9; // the last row is reserved for page control
        if (elements(player).length > this.currentPage * pageSize) {
            registeredInventoryBuilder
                    .item(-4, Item.builder(PAPER)
                            .displayName(text("»", DARK_GRAY))
                            .build(), (clicker, _, _, _) -> open(clicker, this.currentPage + 1));
        }
    }
}

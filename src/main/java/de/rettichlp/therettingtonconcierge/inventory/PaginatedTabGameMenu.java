package de.rettichlp.therettingtonconcierge.inventory;

import de.rettichlp.therettingtonconcierge.inventory.item.Item;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static java.lang.Math.min;
import static java.util.Arrays.copyOfRange;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static org.bukkit.Material.PAPER;

@Getter
public abstract class PaginatedTabGameMenu<E> extends TabGameMenu<E> {

    private int currentPage = 0;
    private int currentTabIndex = 0;

    @Override
    public void builder(Player player, RegisteredInventory.@NonNull Builder registeredInventoryBuilder, int page, int tabIndex) {
        this.currentPage = page;
        this.currentTabIndex = tabIndex;

        // add tab icons
        addTabIcons(registeredInventoryBuilder);

        // add elements for the current tab
        for (E element : getPageElements(player)) {
            registeredInventoryBuilder
                    .add(itemStack(player, element), (clicker, _, clickType, _) -> clickFunction(clicker, clickType, element));
        }

        addPageControl(registeredInventoryBuilder, player);
    }

    @Override
    public void open(Player player) {
        open(player, 1, 0);
    }

    /**
     * Opens the game menu for the specified player at the given page and tab index. This method builds the inventory based on a
     * specified page and tab index, allows for overwriting inventory configurations if applicable, and then opens the inventory for
     * the player.
     *
     * @param player   the player for whom the menu is to be opened
     * @param page     the page number of the menu to display
     * @param tabIndex the tab index number of the menu to display
     */
    public void open(Player player, int page, int tabIndex) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        builder(player, registeredInventoryBuilder, page, tabIndex);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(player);
    }

    private @NonNull @Unmodifiable List<E> getPageElements(Player player) {
        int pageSize = size() - 18; // the first row is reserved for tab control; the last row is reserved for page control
        E[] elements = elements(player, this.currentTabIndex);
        E[] pageElements = copyOfRange(elements, (this.currentPage - 1) * pageSize, min(elements.length, this.currentPage * pageSize));
        return List.of(pageElements);
    }

    private void addPageControl(RegisteredInventory.Builder registeredInventoryBuilder, Player player) {
        if (this.currentPage > 1) {
            registeredInventoryBuilder
                    .item(-6, Item.builder(PAPER)
                            .displayName(text("«", DARK_GRAY))
                            .build(), (clicker, _, _, _) -> open(clicker, this.currentPage - 1, this.currentTabIndex));
        }

        int pageSize = size() - 18; // the first row is reserved for tab control; the last row is reserved for page control
        if (elements(player, this.currentTabIndex).length > this.currentPage * pageSize) {
            registeredInventoryBuilder
                    .item(-4, Item.builder(PAPER)
                            .displayName(text("»", DARK_GRAY))
                            .build(), (clicker, _, _, _) -> open(clicker, this.currentPage + 1, this.currentTabIndex));
        }
    }
}

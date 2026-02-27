package de.rettichlp.therettingtonconcierge.inventory;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static de.rettichlp.therettingtonconcierge.inventory.item.Item.TRANSPARENT_ITEM_STACK;

@Getter
public abstract class TabGameMenu<E> extends GameMenu {

    private int currentTabIndex = 0;

    /**
     * Retrieves a list of {@link ItemStack} instances to be displayed in the current tab of the game menu. The size of the list is
     * limited to a maximum of 9 elements.
     *
     * @return a list of {@link ItemStack} objects representing the items to be displayed in the tab
     */
    public abstract List<ItemStack> tabItems(); // limit to size 9

    /**
     * Retrieves an array of elements to be displayed in the game menu for the specified player and tab index. This method typically
     * returns the elements to be used for constructing the items shown on the menu within the current context of the game.
     *
     * @param player the player for whom the elements are being retrieved
     *
     * @return an array of elements to be displayed in the menu for the given player
     */
    public abstract E[] elements(Player player, int tabIndex);

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
        this.currentTabIndex = tabIndex;

        // add tab icons
        addTabIcons(registeredInventoryBuilder);

        // add elements for the current tab
        for (E element : elements(player, tabIndex)) {
            registeredInventoryBuilder
                    .add(itemStack(player, element), (clicker, _, clickType, _) -> clickFunction(clicker, clickType, element));
        }
    }

    @Override
    public void open(Player player) {
        open(player, 1);
    }

    /**
     * Opens the game menu for the specified player at the given tab index. This method builds the inventory based on a specified tab
     * index, allows for overwriting inventory configurations if applicable, and then opens the inventory for the player.
     *
     * @param player   the player for whom the menu is to be opened
     * @param tabIndex the tab index number of the menu to display
     */
    public void open(Player player, int tabIndex) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        builder(player, registeredInventoryBuilder, 1, tabIndex);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(player);
    }

    void addTabIcons(RegisteredInventory.Builder registeredInventoryBuilder) {
        for (int tabIndex = 0; tabIndex < 9; tabIndex++) {
            boolean isExistingTabItem = tabItems().size() > tabIndex;
            ItemStack itemStack = isExistingTabItem ? tabItems().get(tabIndex) : TRANSPARENT_ITEM_STACK; // use a transparent item stack if there are no items for this tab index

            int finalI = tabIndex;
            registeredInventoryBuilder.item(tabIndex, itemStack, (clicker, _, _, _) -> open(clicker, isExistingTabItem ? finalI : this.currentTabIndex));
        }
    }
}

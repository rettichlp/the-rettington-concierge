package de.rettichlp.therettingtonconcierge.inventory;

import de.rettichlp.therettingtonconcierge.inventory.item.Item;
import de.rettichlp.therettingtonconcierge.ui.dialog.TextInputDialog;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static de.rettichlp.therettingtonconcierge.translation.I18nMiniMessageTranslator.localize;
import static de.rettichlp.therettingtonconcierge.translation.I18nMiniMessageTranslator.localizeMultiline;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.util.Arrays.stream;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.translation.GlobalTranslator.render;
import static org.bukkit.Material.HOPPER;
import static org.bukkit.Material.PAPER;
import static org.bukkit.Material.PLAYER_HEAD;
import static org.bukkit.Material.SPYGLASS;
import static org.bukkit.Material.STRUCTURE_VOID;
import static org.bukkit.event.inventory.ClickType.SHIFT_LEFT;

@Getter
public abstract class PaginatedGameMenu<E> extends GameMenu {

    private int currentPage = 0;
    private String filter = "";
    private String search = "";
    private String sort = "";

    public PaginatedGameMenu(Player player) {
        super(player);
    }

    /**
     * Retrieves an array of elements to be displayed in the paginated menu. The specific type of elements returned depends on the
     * implementation of the method within the subclass of {@code PaginatedGameMenu}.
     *
     * @return an array of elements of type {@code E} to be displayed in the menu
     */
    public abstract E[] elements();

    /**
     * Constructs and returns an {@link ItemStack} representing the provided element. The {@link ItemStack} is used to visually display
     * the element within the paginated menu.
     *
     * @param element the element of type {@code E} for which the {@link ItemStack} needs to be created; must not be null
     *
     * @return an {@link ItemStack} representing the provided element
     */
    public abstract ItemStack itemStack(@NonNull E element);

    /**
     * Handles the click action performed within the paginated game menu. This method is invoked when a user interacts with an element
     * in the menu, providing the specific type of click and the element involved.
     *
     * @param clickType the type of click performed by the user (e.g., left-click, right-click); must not be null
     * @param element   the element of type {@code E} that was clicked; must not be null
     */
    public abstract void clickFunction(ClickType clickType, @NonNull E element);

    /**
     * Builds the inventory structure for the current page, including pagination controls, filters, search, and sorting options. The
     * method dynamically adds elements to the inventory based on the contents of the current page. It also associates click-handling
     * functionality for each element displayed in the inventory.
     *
     * @param registeredInventoryBuilder the builder used to construct the inventory; must not be null
     * @param page                       the current page index to be displayed
     * @param tabIndex                   the currently selected tab index in the inventory
     */
    @Override
    public void builder(RegisteredInventory.@NonNull Builder registeredInventoryBuilder, int page, int tabIndex) {
        this.currentPage = page;

        // add elements for the current page
        for (E element : getPageElements()) {
            registeredInventoryBuilder
                    .add(itemStack(element), (_, _, clickType, _) -> clickFunction(clickType, element));
        }

        addPageControl(registeredInventoryBuilder);
        addPreviousMenuItemStack(registeredInventoryBuilder);
        addFilterItemStack(registeredInventoryBuilder);
        addSearchItemStack(registeredInventoryBuilder);
        addSortItemStack(registeredInventoryBuilder);
    }

    @Override
    public void open() {
        open(1);
    }

    /**
     * Opens the inventory menu at the specified page index for the player. The method builds and initializes the inventory structure,
     * applying modifications if the current instance implements {@link IOverwrite}. Finally, the constructed inventory is displayed to
     * the player.
     *
     * @param page the index of the page to be displayed in the menu; must be a non-negative integer
     */
    public void open(int page) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        builder(registeredInventoryBuilder, page, 0);

        if (this instanceof IOverwrite iOverwrite) {
            iOverwrite.overwrite(registeredInventoryBuilder);
        }

        registeredInventoryBuilder.openInventory(this.player);
    }

    /**
     * Calculates and retrieves the total number of pages required to accommodate all elements in the paginated menu. The number of
     * elements displayed per page is determined by the size of the game menu minus a reserved row for page control.
     *
     * @return the total number of pages as an integer, determined by dividing the count of filtered elements by the effective number
     *         of available slots per page and rounding up
     */
    public int getPageCount() {
        int pageSize = size() - 9; // the last row is reserved for page control
        return (int) ceil((double) getFilteredElements().size() / pageSize);
    }

    /**
     * Filters, searches, and optionally sorts the elements from the menu based on the current filter, search, and sort criteria. If
     * the menu supports filtering and a valid predicate is associated with the filter key, the elements are filtered accordingly. If
     * the menu supports searching and a search term is provided, elements matching the search criteria are included. If the menu
     * supports sorting and a valid comparator is associated with the sort key, the elements are sorted.
     *
     * @return an unmodifiable list of elements of type {@code E} that match the filter, search, and sort criteria
     */
    @SuppressWarnings("unchecked")
    private @NonNull @Unmodifiable List<E> getFilteredElements() {
        Stream<E> elementStream = stream(elements());

        if (this instanceof IFilterable<?> iFilterable && !this.filter.isEmpty()) {
            Predicate<E> predicate = (Predicate<E>) iFilterable.filters().get(this.filter);

            if (predicate != null) {
                elementStream = elementStream.filter(predicate);
            }
        }

        if (this instanceof ISearchable<?> iSearchable && !this.search.isEmpty()) {
            elementStream = elementStream.filter(e -> ((ISearchable<E>) iSearchable).searchFunction(e, this.search));
        }

        if (this instanceof ISortable<?> iSortable && !this.sort.isEmpty()) {
            Comparator<E> comparator = (Comparator<E>) iSortable.comparators().get(this.sort);

            if (comparator != null) {
                elementStream = elementStream.sorted(comparator);
            }
        }

        return elementStream.toList();
    }

    /**
     * Retrieves a sublist of elements to display on the current page of the menu. The elements are filtered, searched, and sorted
     * according to the menu's criteria and then divided into pages based on the available slots per page.
     *
     * @return an unmodifiable list of elements of type {@code E} representing the items to be displayed on the current page of the
     *         menu
     */
    private @NonNull @Unmodifiable List<E> getPageElements() {
        int pageSize = size() - 9; // the last row is reserved for page control
        List<E> elements = getFilteredElements();
        int from = (this.currentPage - 1) * pageSize;
        int to = min(elements.size(), this.currentPage * pageSize);
        return elements.subList(from, to);
    }

    /**
     * Adds pagination controls to the provided inventory builder. These controls allow navigation between pages in a paginated menu.
     * Specifically, this method adds a "previous page" button if the current page is greater than 1, and a "next page" button if there
     * are additional pages available after the current one.
     *
     * @param registeredInventoryBuilder the builder used to construct the inventory; must not be null
     */
    private void addPageControl(RegisteredInventory.Builder registeredInventoryBuilder) {
        if (this.currentPage > 1) {
            registeredInventoryBuilder
                    .item(-6, Item.builder(PAPER)
                            .displayName(text("«", DARK_GRAY))
                            .build(), (_, _, _, _) -> open(this.currentPage - 1));
        }

        if (getPageCount() > this.currentPage) {
            registeredInventoryBuilder
                    .item(-4, Item.builder(PAPER)
                            .displayName(text("»", DARK_GRAY))
                            .build(), (_, _, _, _) -> open(this.currentPage + 1));
        }
    }

    private void addPreviousMenuItemStack(RegisteredInventory.Builder registeredInventoryBuilder) {
        if (this instanceof IPreviousMenu iPreviousMenu) {
            Locale locale = this.player.locale();
            ItemStack previouMenuItemStack = Item.builder(STRUCTURE_VOID)
                    .displayName(localize("gui.back.name", locale))
                    .lore(localizeMultiline("gui.back.tooltip", locale))
                    .customModelData("gui.back")
                    .build();

            registeredInventoryBuilder
                    .item(-9, previouMenuItemStack, (_, _, _, _) -> iPreviousMenu.previousMenu().open());
        }
    }

    /**
     * Adds a filter item stack to the inventory menu, enabling the player to interact with filtering functionality. The filter item
     * visually represents the current filter state and allows toggling between filter options. If the implementing class supports
     * filtering, the filter item stack is dynamically constructed and added to the specified inventory builder.
     *
     * @param registeredInventoryBuilder The inventory builder used to construct the player-visible inventory structure; must not be
     *                                   null.
     */
    private void addFilterItemStack(RegisteredInventory.Builder registeredInventoryBuilder) {
        if (this instanceof IFilterable<?> iFilterable) {
            Locale locale = this.player.locale();
            ItemStack sortItemStack = Item.builder(HOPPER)
                    .displayName(localize("gui.filter.name", locale))
                    .lore(localizeMultiline("gui.filter.tooltip", locale, this.filter.isEmpty() ? "-" : localize("gui.filter.type." + this.filter, locale)))
                    .glint(!this.filter.isEmpty())
                    .build();

            registeredInventoryBuilder
                    .item(-7, sortItemStack, (_, _, clickType, _) -> {
                        if (clickType == SHIFT_LEFT) {
                            this.filter = "";
                            open(this.currentPage); // reopen at the same page
                            return;
                        }

                        this.filter = nextMapKey(iFilterable.filters(), this.filter);
                        open(); // reopen at page 1
                    });
        }
    }

    /**
     * Adds a search item stack to the specified inventory builder, facilitating user interaction with the search functionality of the
     * menu. If the implementing class supports search, this method dynamically creates a search item stack that reflects the current
     * search state and attaches relevant click-handling logic for performing or clearing searches.
     *
     * @param registeredInventoryBuilder the inventory builder used to construct the inventory; must not be null
     */
    private void addSearchItemStack(RegisteredInventory.Builder registeredInventoryBuilder) {
        if (this instanceof ISearchable<?> iSearchable) {
            Locale locale = this.player.locale();
            ItemStack searchItemStack = Item.builder(SPYGLASS)
                    .displayName(localize("gui.search.name", locale))
                    .lore(localizeMultiline("gui.search.tooltip", locale, this.search.isEmpty() ? "-" : this.search))
                    .glint(!this.search.isEmpty())
                    .build();

            registeredInventoryBuilder
                    .item(-5, searchItemStack, (_, _, clickType, _) -> {
                        if (clickType == SHIFT_LEFT) {
                            this.search = "";
                            open(this.currentPage); // reopen at the same page
                            return;
                        }

                        Component title = render(translatable("gui.language.search"), this.player.locale());
                        new TextInputDialog(this.player, title, empty(), s -> {
                            this.search = s;
                            open(); // reopen at page 1
                        }).open();
                    });
        }
    }

    /**
     * Adds a sorting item stack to the specified {@code RegisteredInventory.Builder}. This method is specific to objects implementing
     * the {@code ISortable} interface. The sorting item stack provides functionality for users to change the sorting order or clear
     * the existing sort by interacting with the item.
     *
     * @param registeredInventoryBuilder the {@code RegisteredInventory.Builder} to which the sorting item stack should be added
     */
    private void addSortItemStack(RegisteredInventory.Builder registeredInventoryBuilder) {
        if (this instanceof ISortable<?> iSortable) {
            Locale locale = this.player.locale();
            ItemStack sortItemStack = Item.builder(PLAYER_HEAD)
                    .displayName(localize("gui.sort.name", locale))
                    .lore(localizeMultiline("gui.sort.tooltip", locale, this.sort.isEmpty() ? "-" : localize("gui.sort.type." + this.sort, locale)))
                    .skullTexture("f5a19af0e61ca42532c0599fa0a391753df6b71f9fa4a177f1aa9b1d81fe6ee2")
                    .glint(!this.sort.isEmpty())
                    .build();

            registeredInventoryBuilder
                    .item(-3, sortItemStack, (_, _, clickType, _) -> {
                        if (clickType == SHIFT_LEFT) {
                            this.sort = "";
                            open(this.currentPage); // reopen at the same page
                            return;
                        }

                        this.sort = nextMapKey(iSortable.comparators(), this.sort);
                        open(this.currentPage); // reopen at the same page because the sorting does not change the number of elements
                    });
        }
    }

    /**
     * Retrieves the next key in the provided map based on the current key. If the current key is empty, returns the first key in the
     * map. If the current key is the last key in the map, returns an empty string.
     *
     * @param map        the map containing the keys to iterate through; must not be null
     * @param currentKey the current key from which to find the next key; must not be null
     *
     * @return the next key in the map if it exists, or an empty string if the current key is the last key
     */
    private String nextMapKey(@NonNull Map<String, ?> map, @NonNull String currentKey) {
        List<String> keys = new ArrayList<>(map.keySet());

        if (currentKey.isEmpty()) {
            return keys.getFirst();
        }

        int nextIndex = keys.indexOf(currentKey) + 1;
        return nextIndex >= keys.size() ? "" : keys.get(nextIndex);
    }
}

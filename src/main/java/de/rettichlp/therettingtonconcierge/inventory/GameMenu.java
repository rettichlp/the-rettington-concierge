package de.rettichlp.therettingtonconcierge.inventory;

import de.rettichlp.therettingtonconcierge.inventory.item.Item;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static de.rettichlp.therettingtonconcierge.translation.I18nMiniMessageTranslator.localize;
import static de.rettichlp.therettingtonconcierge.utils.ThreadUtils.runOnMainThread;
import static java.util.logging.Level.SEVERE;
import static net.kyori.adventure.text.Component.translatable;
import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Material.STRUCTURE_VOID;

public abstract class GameMenu {

    protected final Player player;

    private RegisteredInventory registeredInventory;
    private Object loadedData;

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
        if (this instanceof ILoadable<?> iLoadable) {
            openLoading(iLoadable);
            return;
        }

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
     * @return {@code true} if the content of the open menu was updated, {@code false} if the menu is not open
     */
    protected boolean refreshAt(int page) {
        if (!isOpen()) {
            return false;
        }

        createBuilder(page).updateInventory(this.registeredInventory);
        return true;
    }

    /**
     * Returns the data that an {@link ILoadable} menu loaded. It is there when {@link #builder(RegisteredInventory.Builder, int, int)}
     * and {@link IOverwrite} run, and {@code null} before that. The type is the type parameter of the {@link ILoadable} the menu
     * implements. Asking for another type fails with a {@link ClassCastException} where the data is used.
     *
     * @param <T> the type of the data
     * @return the loaded data, or {@code null} while it loads or if the menu is not {@link ILoadable}
     */
    @SuppressWarnings("unchecked")
    protected <T> T loaded() {
        return (T) this.loadedData;
    }

    /**
     * Opens the menu with a loading icon instead of its content and, as soon as the data is there, hands it to the menu and shows the
     * content in the same inventory. If the data is there already, the menu opens directly with the content. The result is dropped if
     * the player closed the menu or opened another one in the meantime.
     */
    private <T> void openLoading(@NonNull ILoadable<T> loadable) {
        this.loadedData = null;
        CompletableFuture<T> future = loadable.load();

        if (future.isDone() && !future.isCompletedExceptionally()) {
            this.loadedData = future.join();
            openAt(1);
            return;
        }

        openAt(1); // shows the loading icon, because the data is not there yet

        future.whenComplete((data, throwable) -> runOnMainThread(() -> {
            // the player closed the loading screen or opened something else
            if (!isOpen()) {
                return;
            }

            if (throwable != null) {
                getLogger().log(SEVERE, "Failed to load the menu data for " + this.player.getName(), throwable);
                this.player.closeInventory();
                this.player.sendMessage(translatable("gui.error.load"));
                return;
            }

            this.loadedData = data;
            refresh(); // replaces the loading icon in the open inventory
        }));
    }

    private RegisteredInventory.Builder createBuilder(int page) {
        RegisteredInventory.Builder registeredInventoryBuilder = RegisteredInventory.getBuilder()
                .title(title())
                .size(size());

        // an ILoadable menu shows the loading icon until its data is there
        if (this instanceof ILoadable<?> && this.loadedData == null) {
            Locale locale = this.player.locale();
            ItemStack loadingItemStack = Item.builder(STRUCTURE_VOID)
                    .displayName(localize("gui.loading.name", locale))
                    .customModelData("gui.loading")
                    .build();

            // the middle of the middle row
            registeredInventoryBuilder.item(size() / 9 / 2 * 9 + 4, loadingItemStack);
            return registeredInventoryBuilder;
        }

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

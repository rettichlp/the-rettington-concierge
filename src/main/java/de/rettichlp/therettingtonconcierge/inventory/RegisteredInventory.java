package de.rettichlp.therettingtonconcierge.inventory;

import de.rettichlp.therettingtonconcierge.inventory.item.Item;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.minecraft.core.NonNullList;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.rettichlp.therettingtonconcierge.inventory.item.Item.TRANSPARENT_ITEM_STACK;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.stream.IntStream.range;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;
import static net.minecraft.core.NonNullList.withSize;
import static org.bukkit.Bukkit.createInventory;
import static org.bukkit.Material.AIR;
import static org.bukkit.Sound.BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF;

@Data
public class RegisteredInventory {

    private final Component title;
    private final Inventory inventory;
    private final NonNullList<InventorySlot> inventorySlots;
    private final Sound clickSound;
    private final boolean allowPlayerInventoryInteraction;
    private final InventoryClosedFunction<Player, Inventory, Reason> closedFunction;
    private boolean open;

    /**
     * Retrieves the title of the registered inventory in plain text format.
     *
     * @return the serialized plain text representation of the title
     */
    public String getPlainTitle() {
        return plainText().serialize(this.title);
    }

    /**
     * Creates and returns a new instance of the Builder class for constructing objects related to RegisteredInventory.
     *
     * @return a new instance of Builder
     */
    @Contract(" -> new")
    public static @NonNull Builder getBuilder() {
        return new Builder();
    }

    /**
     * Represents a functional interface that defines an action to be performed on a specific inventory slot within a registered
     * inventory system. This action may involve interaction between a player, the inventory, and a specific type of click input or
     * additional data.
     *
     * @param <Player>    the type of the entity who interacts with the inventory, such as a player or NPC
     * @param <Inventory> the type of the inventory being interacted with
     * @param <ClickType> the type of click interaction performed, such as left click, right click, etc.
     * @param <T>         the type of additional data required for the action
     */
    public interface InventorySlotFunction<Player, Inventory, ClickType, T> {

        /**
         * Executes a specific operation or action based on the given parameters, which represents a functional interface's core
         * behavior for interacting with inventory slots. This method is invoked when interacting with a slot in a custom inventory
         * system.
         *
         * @param player    the player interacting with the inventory, representing the user's context
         * @param inventory the inventory object being interacted with
         * @param clickType the type of click action performed within the inventory
         * @param t         an additional parameter to handle context-specific logic
         */
        void apply(Player player, Inventory inventory, ClickType clickType, T t);
    }

    /**
     * Represents a functional interface for defining a custom action that is executed when an inventory is closed. This interface
     * supports specifying the entities and context involved in the operation, such as the player, the inventory being closed, and the
     * associated reason.
     *
     * @param <Player>    the type representing the entity performing or associated with the action
     * @param <Inventory> the type representing the inventory being operated on
     * @param <Reason>    the type representing the reason or context for the operation
     */
    public interface InventoryClosedFunction<Player, Inventory, Reason> {

        /**
         * Executes a specific action using the provided player, inventory, and reason.
         *
         * @param player    the player involved in the operation
         * @param inventory the inventory to be operated on
         * @param reason    the reason or context associated with this operation
         */
        void apply(Player player, Inventory inventory, Reason reason);
    }

    /**
     * The {@code Builder} class is responsible for constructing and managing inventory-based configurations. It allows setting up
     * inventory properties, items, and interactions. This class provides a fluent API for creating custom inventories with various
     * settings and components.
     */
    @NoArgsConstructor
    public static class Builder {

        /**
         * A static collection that holds all instances of {@link RegisteredInventory}. This collection is used to track and manage the
         * set of inventories that have been registered within the application.
         * <p>
         * It is initialized as an {@link ArrayList} and is modifiable unless additional restrictions or synchronization are applied
         * elsewhere in the codebase.
         * <p>
         * Thread-safety must be ensured externally if this collection is accessed or modified concurrently.
         */
        public static final Collection<RegisteredInventory> REGISTERED_INVENTORIES = new ArrayList<>();

        private static final ItemStack CONSTRUCTION_HELPER_ITEM_STACK = Item.builder(AIR).build();

        private int size = 54;
        private NonNullList<InventorySlot> inventorySlots = withSize(54, new InventorySlot(CONSTRUCTION_HELPER_ITEM_STACK, true, null));
        private Component title = empty();
        private Sound openSound;
        private Sound clickSound = BLOCK_WOODEN_PRESSURE_PLATE_CLICK_OFF;
        private boolean allowPlayerInventoryInteraction = false;
        private InventoryClosedFunction<Player, Inventory, Reason> closedFunction = (_, _, _) -> {
        };

        /**
         * Sets the size of the inventory and updates the inventory slots accordingly.
         *
         * @param size the size to set for the inventory
         *
         * @return the Builder instance with the updated size
         */
        public Builder size(int size) {
            this.size = size;
            this.inventorySlots = withSize(size, new InventorySlot(CONSTRUCTION_HELPER_ITEM_STACK, true, null));
            return this;
        }

        /**
         * Adjusts the size dynamically based on the given number of items. Ensures the size is a multiple of 9 and does not exceed
         * 54.
         *
         * @param items the number of items to determine the required size
         *
         * @return the Builder instance with the updated size
         */
        public Builder dynamicSize(int items) {
            int remainder = items % 9;
            int requiredSize = remainder == 0 ? items : (items + 9 - remainder);
            return size(min(requiredSize, 54));
        }

        /**
         * Sets the title of the builder with the provided text styled in gray.
         *
         * @param title the text to be used as the title
         *
         * @return the updated Builder instance with the title set
         */
        public Builder title(String title) {
            return title(text(title, GRAY));
        }

        /**
         * Sets the title of the component.
         *
         * @param component the component to be used as the title
         *
         * @return the updated Builder instance
         */
        public Builder title(Component component) {
            this.title = component;
            return this;
        }

        /**
         * Sets the sound to be played when a specific event occurs or when opening something.
         *
         * @param sound the Sound object representing the sound to be played
         *
         * @return the Builder instance for method chaining
         */
        public Builder openSound(Sound sound) {
            this.openSound = sound;
            return this;
        }

        /**
         * Sets the sound to be played when a click action occurs.
         *
         * @param sound the sound to be played on click
         *
         * @return the builder instance for chaining
         */
        public Builder clickSound(Sound sound) {
            this.clickSound = sound;
            return this;
        }

        /**
         * Sets whether the player is allowed to interact with their inventory.
         *
         * @param allow a boolean indicating if player inventory interaction should be allowed
         *
         * @return the Builder instance for method chaining
         */
        public Builder allowPlayerInventoryInteraction(boolean allow) {
            this.allowPlayerInventoryInteraction = allow;
            return this;
        }

        /**
         * Adds an item stack to the first available empty inventory slot and associates an inventory slot function.
         *
         * @param itemStack             The item stack to be added to the inventory.
         * @param inventorySlotFunction A function that determines the slots to which the item stack should be associated within the
         *                              inventory.
         *
         * @return The Builder instance for chainable method calls.
         *
         * @throws IllegalStateException If no empty inventory slot is available.
         */
        public Builder add(ItemStack itemStack,
                           InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            // first empty slot
            int firstEmptySlotIndex = range(0, this.inventorySlots.size()).filter(value -> this.inventorySlots.get(value).itemStack().isSimilar(CONSTRUCTION_HELPER_ITEM_STACK))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No empty inventory slot found"));

            return item(firstEmptySlotIndex, itemStack, true, inventorySlotFunction);
        }

        /**
         * Sets an item with the specified properties at the given index.
         *
         * @param index     the index where the item should be set
         * @param material  the material of the item
         * @param component the component associated with the item
         *
         * @return the builder instance, allowing for method chaining
         */
        public Builder item(int index, Material material, Component component) {
            return item(index, material, component, new ArrayList<>(), true, null);
        }

        /**
         * Adds an item to the builder with the specified properties.
         *
         * @param index                 The index at which the item should be placed.
         * @param material              The material of the item.
         * @param component             The component representing the visual or textual description of the item.
         * @param inventorySlotFunction The function to handle the interaction with the inventory slots for the item.
         *
         * @return The builder instance, allowing for method chaining.
         */
        public Builder item(int index,
                            Material material,
                            Component component,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            return item(index, material, component, new ArrayList<>(), true, inventorySlotFunction);
        }

        /**
         * Sets an item in the builder with the specified parameters.
         *
         * @param index     the index of the item to set
         * @param material  the material of the item
         * @param component the display name or main component of the item
         * @param lore      the list of components representing the item's lore or description
         *
         * @return the builder instance with the updated item
         */
        public Builder item(int index, Material material, Component component, List<Component> lore) {
            return item(index, material, component, lore, null);
        }

        /**
         * Configures an inventory slot at the specified position with the provided material, display name, and lore. This method
         * creates an item stack based on the given parameters and applies it to the slot, with additional configurations.
         *
         * @param index     the position where the inventory slot should be set; supports negative values to count positions from the
         *                  end of the inventory
         * @param material  the material of the item to be placed in the specified slot
         * @param component the display name of the item, represented as a {@link Component}
         * @param lore      an array of {@link Component} instances representing the lore of the item
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index, Material material, Component component, Component[] lore) {
            return item(index, material, component, asList(lore), true, null);
        }

        /**
         * Configures an inventory slot at the specified position with the provided material, display name, lore, and click behavior.
         * This method constructs an item stack and assigns it to the slot with additional configurations for click actions and custom
         * slot functionality.
         *
         * @param index                 the position where the inventory slot should be set; supports negative values to count
         *                              positions from the end of the inventory
         * @param material              the material of the item to be placed in the specified slot
         * @param component             the display name of the item, represented as a {@link Component}
         * @param lore                  an array of {@link Component} instances representing the lore of the item
         * @param inventorySlotFunction a functional interface that defines the behavior when the slot is clicked, providing access to
         *                              the player, inventory, click type, and additional slot information
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index,
                            Material material,
                            Component component,
                            Component[] lore,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            return item(index, material, component, asList(lore), true, inventorySlotFunction);
        }

        /**
         * Configures an inventory slot at the specified position with the provided material, display name, lore, and click behavior.
         * This method constructs an item stack and assigns it to the slot with default click cancellation behavior and additional slot
         * functionalities.
         *
         * @param index                 the position where the inventory slot should be set; supports negative values to count
         *                              positions from the end of the inventory
         * @param material              the material of the item to be placed in the specified slot
         * @param component             the display name of the item, represented as a {@link Component}
         * @param lore                  the lore of the item, a list of {@link Component} instances representing additional descriptive
         *                              text
         * @param inventorySlotFunction a functional interface that defines the behavior when the slot is clicked, providing access to
         *                              the player, inventory, click type, and additional slot information
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index,
                            Material material,
                            Component component,
                            List<Component> lore,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            return item(index, material, component, lore, true, inventorySlotFunction);
        }

        /**
         * Configures an inventory slot at the specified position with the provided material, display name, lore, and click behavior.
         * This method constructs an item stack and assigns it to the slot with additional configurations for click actions and custom
         * slot functionality.
         *
         * @param index                 the position where the inventory slot should be set; supports negative values to count
         *                              positions from the end of the inventory
         * @param material              the material of the item to be placed in the specified slot
         * @param component             the display name of the item, represented as a {@link Component}
         * @param lore                  the lore of the item, a list of {@link Component} instances representing additional descriptive
         *                              text
         * @param cancelClick           a flag indicating whether to cancel the click action when this slot is interacted with
         * @param inventorySlotFunction a functional interface that defines the behavior when the slot is clicked, providing access to
         *                              the player, inventory, click type, and additional slot information
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index,
                            Material material,
                            Component component,
                            List<Component> lore,
                            boolean cancelClick,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            ItemStack itemStack = Item.builder(material)
                    .displayName(component)
                    .lore(lore)
                    .build();

            return item(index, itemStack, cancelClick, inventorySlotFunction);
        }

        /**
         * Sets an inventory slot at the specified position with the provided item stack and default click handling behavior. This
         * method supports negative indices to count from the end of the inventory.
         *
         * @param index     the position where the inventory slot should be set; supports negative values to count positions from the
         *                  end of the inventory
         * @param itemStack the item stack to be placed at the specified slot
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index, ItemStack itemStack) {
            return item(index, itemStack, true, null);
        }

        /**
         * Sets an inventory slot at the specified position with the provided item stack and click cancellation behavior. The method
         * supports negative indices to count from the end of the inventory and allows configuration of item interactions.
         *
         * @param index       the position where the inventory slot should be set; supports negative values to count positions from the
         *                    end of the inventory
         * @param itemStack   the item stack to be placed at the specified slot
         * @param cancelClick a flag indicating whether to cancel the click action when this slot is interacted with
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index, ItemStack itemStack, boolean cancelClick) {
            return item(index, itemStack, cancelClick, null);
        }

        /**
         * Sets an inventory slot at the specified position with the provided item stack and slot function. This method supports
         * specifying the behavior for slot interactions and allows chaining for further configuration.
         *
         * @param index                 the position where the inventory slot should be set; supports negative values to count
         *                              positions from the end of the inventory
         * @param itemStack             the item stack to be placed at the specified slot
         * @param inventorySlotFunction a functional interface that defines the behavior when the slot is clicked, providing access to
         *                              the player, inventory, click type, and additional slot information
         *
         * @return the Builder instance for method chaining
         */
        public Builder item(int index,
                            ItemStack itemStack,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            return item(index, itemStack, true, inventorySlotFunction);
        }

        /**
         * Sets an inventory slot at the specified position with the provided item stack, click behavior, and slot function. The method
         * supports negative indices to count from the end of the inventory and allows customization of click handling.
         *
         * @param index                 the position where the inventory slot should be set; supports negative values to count
         *                              positions from the end of the inventory
         * @param itemStack             the item stack to be placed at the specified slot
         * @param cancelClick           a flag indicating whether to cancel the click action when this slot is interacted with
         * @param inventorySlotFunction a functional interface that defines the behavior when the slot is clicked, providing access to
         *                              the player, inventory, click type, and additional slot information
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder item(int index,
                            ItemStack itemStack,
                            boolean cancelClick,
                            InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {
            if (index < 0) {
                index = this.size + index;
            }

            InventorySlot inventorySlot = new InventorySlot(itemStack, cancelClick, inventorySlotFunction);

            this.inventorySlots.set(index, inventorySlot);
            return this;
        }

        /**
         * Sets a function to be executed when an inventory is closed. The provided function defines the behavior that occurs when the
         * inventory is closed, including any actions or side effects involving the player, inventory, and the reason for closure.
         *
         * @param closedFunction the function to execute when the inventory is closed, which takes the player, inventory, and reason as
         *                       parameters
         *
         * @return the {@code Builder} instance for method chaining
         */
        public Builder onClosed(InventoryClosedFunction<Player, Inventory, Reason> closedFunction) {
            this.closedFunction = closedFunction;
            return this;
        }

        /**
         * Builds and registers an inventory with the specified configurations. This includes setting up inventory size, items, slots,
         * and other attributes. The inventory is returned as a {@link RegisteredInventory} object.
         *
         * @param holder the player who will be associated with the inventory, or null if no specific player association is needed.
         *
         * @return the constructed {@link RegisteredInventory} instance that has been registered and configured with the initial
         *         setup.
         *
         * @throws IllegalStateException if the size or pattern for the inventory is not set.
         */
        public RegisteredInventory build(@Nullable Player holder) {
            if (this.size == 0) {
                throw new IllegalStateException("Either size or pattern must be set");
            }

            List<InventorySlot> inventorySlotsWithoutConstructionHelperItemStack = this.inventorySlots.stream().map(inventorySlot -> inventorySlot.itemStack().isSimilar(CONSTRUCTION_HELPER_ITEM_STACK)
                    ? new InventorySlot(TRANSPARENT_ITEM_STACK, true, null)
                    : inventorySlot).toList();

            Inventory inventory = createInventory(holder, this.size, this.title);
            for (int i = 0; i < this.inventorySlots.size(); i++) {
                InventorySlot inventorySlot = inventorySlotsWithoutConstructionHelperItemStack.get(i);
                inventory.setItem(i, inventorySlot.itemStack());
            }

            RegisteredInventory registeredInventory = new RegisteredInventory(this.title, inventory, this.inventorySlots, this.clickSound, this.allowPlayerInventoryInteraction, this.closedFunction);
            REGISTERED_INVENTORIES.add(registeredInventory);

            return registeredInventory;
        }

        /**
         * Opens an inventory for the specified player and plays a sound effect.
         *
         * @param player the player for whom the inventory will be opened
         */
        public void openInventory(Player player) {
            RegisteredInventory registeredInventory = build(player);
            registeredInventory.setOpen(true);
            player.openInventory(registeredInventory.getInventory());
            player.playSound(player.getLocation(), this.openSound, 1, 1);
        }
    }

    /**
     * Represents a slot in an inventory within the game menu system. This class encapsulates the properties and behavior associated
     * with an individual slot, including the item contained in the slot, whether the slot cancels click interactions, and a custom
     * behavior function for handling interactions.
     *
     * @param itemStack             the item contained in the inventory slot
     * @param cancelClick           a flag indicating whether click interactions should be canceled for this slot
     * @param inventorySlotFunction an optional functional interface defining custom behavior when interacting with the inventory slot
     */
    public record InventorySlot(ItemStack itemStack, boolean cancelClick, @Nullable InventorySlotFunction<Player, Inventory, ClickType, Collection<InventorySlot>> inventorySlotFunction) {

    }
}

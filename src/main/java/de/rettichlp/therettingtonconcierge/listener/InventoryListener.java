package de.rettichlp.therettingtonconcierge.listener;

import com.google.inject.Inject;
import de.rettichlp.therettingtonconcierge.inventory.RegisteredInventory;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static de.rettichlp.therettingtonconcierge.inventory.RegisteredInventory.Builder.REGISTERED_INVENTORIES;
import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;
import static org.bukkit.Material.BLACK_STAINED_GLASS_PANE;
import static org.bukkit.Material.WHITE_STAINED_GLASS_PANE;
import static org.bukkit.event.EventPriority.HIGH;
import static org.bukkit.event.EventPriority.HIGHEST;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
public class InventoryListener implements Listener {

    private static final Map<Player, Long> SPAM_PREVENTION = new HashMap<>();

    private final JavaPlugin plugin;

    @EventHandler(priority = HIGH)
    public void onInventoryClick(@NonNull InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Inventory inventory = event.getClickedInventory();
        ItemStack currentItem = event.getCurrentItem();
        HumanEntity clicker = event.getWhoClicked();

        if (!(clicker instanceof Player player)) {
            return;
        }

        // registered inventory
        Optional<RegisteredInventory> optionalRegisteredInventory = REGISTERED_INVENTORIES.stream()
                .filter(registeredInventory -> registeredInventory.getInventory().equals(topInventory))
                .findFirst();

        if (optionalRegisteredInventory.isPresent()) {
            RegisteredInventory registeredInventory = optionalRegisteredInventory.get();

            if (!Objects.equals(topInventory, inventory)) { // clicked inventory is not the top inventory (e.g., player inventory)
                event.setCancelled(!registeredInventory.isAllowPlayerInventoryInteraction());
                return;
            }

            ClickType click = event.getClick();
            if (currentItem != null) {
                int slotIndex = event.getSlot();
                if (slotIndex >= registeredInventory.getInventory().getSize()) {
                    throw new IndexOutOfBoundsException("Slot index is higher than inventory size");
                }

                // cancel click (if required) before handling it or aborting due to spam prevention
                RegisteredInventory.InventorySlot clickedInventorySlot = registeredInventory.getInventorySlots().get(slotIndex);
                event.setCancelled(clickedInventorySlot.cancelClick());

                // spam prevention
                if (currentTimeMillis() - SPAM_PREVENTION.getOrDefault(player, 0L) < 200) {
                    return;
                }

                SPAM_PREVENTION.put(player, currentTimeMillis());

                ofNullable(clickedInventorySlot.inventorySlotFunction())
                        .ifPresent(inventoryItemFunction -> inventoryItemFunction.apply(player, registeredInventory.getInventory(), click, registeredInventory.getInventorySlots()));

                Sound sound = registeredInventory.getClickSound();
                Material type = currentItem.getType();
                if (!type.isAir() && type != WHITE_STAINED_GLASS_PANE && type != BLACK_STAINED_GLASS_PANE && sound != null) {
                    player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
                }
            }
        }
    }

    @EventHandler(priority = HIGHEST)
    public void onInventoryOpen(@NonNull InventoryOpenEvent event) {
        getOptionalRegisteredInventory(event.getInventory())
                .ifPresent(registeredInventory -> registeredInventory.setOpen(true));
    }

    @EventHandler(priority = HIGHEST)
    public void onInventoryClose(@NonNull InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryCloseEvent.Reason reason = event.getReason();

        Optional<RegisteredInventory> optionalRegisteredInventory = getOptionalRegisteredInventory(inventory);
        optionalRegisteredInventory.ifPresent(registeredInventory -> registeredInventory.setOpen(false));

        Optional<RegisteredInventory.InventoryClosedFunction<Player, Inventory, InventoryCloseEvent.Reason>> optionalInventoryCloseFunction = optionalRegisteredInventory
                .map(RegisteredInventory::getClosedFunction);

        if (optionalInventoryCloseFunction.isPresent() && event.getPlayer() instanceof Player player) {
            RegisteredInventory.InventoryClosedFunction<Player, Inventory, InventoryCloseEvent.Reason> inventoryClosedFunction = optionalInventoryCloseFunction.get();
            inventoryClosedFunction.apply(player, inventory, reason);
        }
    }

    private @NonNull Optional<RegisteredInventory> getOptionalRegisteredInventory(Inventory inventory) {
        return REGISTERED_INVENTORIES.stream()
                .filter(registeredInventory -> registeredInventory.getInventory().equals(inventory))
                .findFirst();
    }
}

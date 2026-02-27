package de.rettichlp.therettingtonconcierge.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@Getter
public class PlayerHotbarItemFocusLostEvent extends PlayerHotbarItemFocusEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public PlayerHotbarItemFocusLostEvent(Player player, @Nullable ItemStack itemStack) {
        super(player, itemStack);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

package de.rettichlp.therettingtonconcierge.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@Getter
public class PlayerMoveBlockEvent extends PlayerEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Nullable
    private final Block previousBlock;

    public PlayerMoveBlockEvent(Player player, @Nullable Block previousBlock) {
        super(player);
        this.previousBlock = previousBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

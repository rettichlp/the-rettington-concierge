package de.rettichlp.therettingtonconcierge.events;

import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

@Getter
public class PlayerMoveChunkEvent extends PlayerEvent {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    @Nullable
    private final Chunk previousChunk;

    public PlayerMoveChunkEvent(Player player, @Nullable Chunk previousChunk) {
        super(player);
        this.previousChunk = previousChunk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

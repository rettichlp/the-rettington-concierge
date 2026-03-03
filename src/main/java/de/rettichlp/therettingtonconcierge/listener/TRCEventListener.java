package de.rettichlp.therettingtonconcierge.listener;

import de.rettichlp.therettingtonconcierge.events.PlayerHotbarItemFocusGetEvent;
import de.rettichlp.therettingtonconcierge.events.PlayerHotbarItemFocusLostEvent;
import de.rettichlp.therettingtonconcierge.events.PlayerMoveBlockEvent;
import de.rettichlp.therettingtonconcierge.events.PlayerMoveChunkEvent;
import io.papermc.paper.event.packet.ClientTickEndEvent;
import lombok.NoArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getPluginManager;

@NoArgsConstructor
public class TRCEventListener implements Listener {

    private static final Map<Player, Block> PLAYER_PREVIOUS_BLOCK = new HashMap<>();
    private static final Map<Player, Chunk> PLAYER_PREVIOUS_CHUNK = new HashMap<>();
    private static final Map<Player, ItemStack> LAST_HELD_ITEM = new HashMap<>();

    @EventHandler
    public void onPlayerMove(@NonNull PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // check block
        Block currentBlock = player.getLocation().getBlock();
        Block previousBlock = PLAYER_PREVIOUS_BLOCK.get(player);

        if (!currentBlock.equals(previousBlock)) {
            getPluginManager().callEvent(new PlayerMoveBlockEvent(player, previousBlock));
            PLAYER_PREVIOUS_BLOCK.put(player, currentBlock);
        }

        Chunk currentChunk = player.getChunk();
        Chunk previousChunk = PLAYER_PREVIOUS_CHUNK.get(player);

        if (!currentChunk.equals(previousChunk)) {
            getPluginManager().callEvent(new PlayerMoveChunkEvent(player, previousChunk));
            PLAYER_PREVIOUS_CHUNK.put(player, currentChunk);
        }
    }

    @EventHandler
    public void onClientTickEnd(@NonNull ClientTickEndEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();

        ItemStack lastHeldItem = LAST_HELD_ITEM.get(player);
        if (itemStack.equals(lastHeldItem)) {
            return;
        }

        getPluginManager().callEvent(new PlayerHotbarItemFocusLostEvent(player, lastHeldItem));
        getPluginManager().callEvent(new PlayerHotbarItemFocusGetEvent(player, itemStack));
        LAST_HELD_ITEM.put(player, itemStack);
    }
}

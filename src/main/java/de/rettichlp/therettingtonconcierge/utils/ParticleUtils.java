package de.rettichlp.therettingtonconcierge.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NonNull;

import static org.bukkit.Particle.DUST;

@Getter
@AllArgsConstructor
public class ParticleUtils {

    /**
     * Draws a particle-based line between two locations that is visible to specific players.
     *
     * @param startLocation     The starting location of the line. Must not be null.
     * @param endLocation       The ending location of the line.
     * @param color             The color of the particle line.
     * @param visibleForPlayers The players for whom the particle line will be visible.
     */
    public static void drawLine(@NonNull Location startLocation, Location endLocation, Color color, Player... visibleForPlayers) {
        double distance = startLocation.distance(endLocation);
        long particleCount = (long) (distance * 10);

        for (int i = 0; i <= particleCount; i++) {
            double particleLocationModifier = i / (double) particleCount;

            Vector vector = endLocation.toVector().subtract(startLocation.toVector()).multiply(particleLocationModifier);
            Location location = startLocation.add(vector);

            for (Player visibleForPlayer : visibleForPlayers) {
                visibleForPlayer.spawnParticle(DUST, location, 1, new Particle.DustOptions(color, 0.4f));
            }
        }
    }

    /**
     * Draws the edges of a block as particle lines based on the given block location, color, and the players for whom the edges should
     * be visible.
     *
     * @param blockLocation     The location of the block whose edges are to be drawn. Must not be null.
     * @param color             The color of the particle lines representing the edges.
     * @param visibleForPlayers The players for whom the particle edges will be visible.
     */
    public static void drawBlockEdges(Location blockLocation, Color color, Player... visibleForPlayers) {
        double[][] edges = {
                // Bottom square
                { 0, 0, 0 }, { 1, 0, 0 },
                { 1, 0, 0 }, { 1, 0, 1 },
                { 1, 0, 1 }, { 0, 0, 1 },
                { 0, 0, 1 }, { 0, 0, 0 },

                // Top square
                { 0, 1, 0 }, { 1, 1, 0 },
                { 1, 1, 0 }, { 1, 1, 1 },
                { 1, 1, 1 }, { 0, 1, 1 },
                { 0, 1, 1 }, { 0, 1, 0 },

                // Vertical edges
                { 0, 0, 0 }, { 0, 1, 0 },
                { 1, 0, 0 }, { 1, 1, 0 },
                { 1, 0, 1 }, { 1, 1, 1 },
                { 0, 0, 1 }, { 0, 1, 1 }
        };

        for (int i = 0; i < edges.length; i += 2) {
            Location startLocation = blockLocation.clone().add(edges[i][0], edges[i][1], edges[i][2]);
            Location endLocation = blockLocation.clone().add(edges[i + 1][0], edges[i + 1][1], edges[i + 1][2]);
            drawLine(startLocation, endLocation, color, visibleForPlayers);
        }
    }
}

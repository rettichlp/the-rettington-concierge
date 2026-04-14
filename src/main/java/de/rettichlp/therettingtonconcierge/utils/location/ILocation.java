package de.rettichlp.therettingtonconcierge.utils.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;

public interface ILocation {

    /**
     * Retrieves a {@link Location} object representing the specific location in the given world.
     *
     * @param world the world for which the location is retrieved; must not be null
     *
     * @return a non-null {@link Location} object corresponding to the specified world
     */
    @NonNull Location getLocation(World world);

    default void teleport(@NonNull Entity entity) {
        teleport(entity, entity.getWorld());
    }

    default void teleport(@NonNull Entity entity, World world) {
        entity.teleport(getLocation(world));
    }

    default double distance(@NonNull Entity entity) {
        return distance(entity.getLocation());
    }

    default double distance(@NonNull Location other) {
        return other.distance(getLocation(other.getWorld()));
    }
}

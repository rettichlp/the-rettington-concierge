package de.rettichlp.therettingtonconcierge.utils.location;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;

public interface ILocation {

    /**
     * Retrieves the {@link Location} associated with the current object.
     *
     * @return A non-null {@code Location} instance representing the object's location.
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

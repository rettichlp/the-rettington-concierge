package de.rettichlp.therettingtonconcierge.utils.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

import static java.util.Comparator.comparingDouble;

@Getter
@AllArgsConstructor
public class LocationUtils {

    /**
     * Calculates and returns the centered location of the given entity, aligning its yaw to match the Cartesian cardinal facing
     * direction (North, East, South, West).
     *
     * @param entity the non-null entity whose location and facing direction are used to determine the resultant location.
     *
     * @return a non-null {@link Location} object representing the centered location of the entity with adjusted yaw based on its
     *         facing direction.
     *
     * @throws IllegalStateException if the entity's facing direction is not one of the expected values (North, East, South, West).
     */
    public static @NonNull Location getLocationWithCartesianFacing(@NonNull Entity entity) {
        Location location = entity.getLocation().clone().toCenterLocation();
        BlockFace facing = entity.getFacing();

        location.setPitch(0.0f);
        location.setYaw(switch (facing) {
            case NORTH -> 180.0f;
            case EAST -> 270.0f;
            case SOUTH -> 0.0f;
            case WEST -> 90.0f;
            default -> throw new IllegalStateException("Unexpected value: " + facing);
        });

        return location;
    }

    /**
     * Finds and returns the nearest element to the given reference location from the provided collection of elements. The distance is
     * calculated based on the squared distance between the location of each element and the reference location.
     *
     * @param referenceLocation the reference {@link Location} to which the distance is measured.
     * @param elements          a non-null collection of elements implementing {@link ILocation}, each providing its own location. The
     *                          collection can be empty.
     * @param <T>               the type of elements in the collection, which must extend {@link ILocation}.
     *
     * @return the nearest element of type {@code T} to the specified reference location, or {@code null} if the collection is empty.
     */
    public static @Nullable <T extends ILocation> T getNearest(Location referenceLocation, @NonNull Collection<T> elements) {
        return elements.stream()
                .min(comparingDouble(element -> element.getLocation().distanceSquared(referenceLocation)))
                .orElse(null);
    }
}

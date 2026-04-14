package de.rettichlp.therettingtonconcierge.utils.location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.Comparator.comparingDouble;
import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

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
     * Finds the nearest element from a given iterable of elements that implement the {@link ILocation} interface, based on the squared
     * distance to the specified location.
     *
     * @param location the reference location used to calculate distances; must not be null
     * @param elements the iterable collection of elements to search through; must not be null
     * @param <T>      the type of elements in the iterable, which must extend {@link ILocation}
     *
     * @return the nearest element from the iterable based on squared distance to the location, or {@code null} if the iterable is
     *         empty
     */
    public static <T extends ILocation> @Nullable T getNearest(@NonNull Location location, @NonNull Iterable<T> elements) {
        World world = location.getWorld();
        double maxDistance = POSITIVE_INFINITY;
        T nearest = null;

        for (T element : elements) {
            double distance = location.distanceSquared(element.getLocation(world));
            if (distance < maxDistance) {
                maxDistance = distance;
                nearest = element;
            }
        }

        return nearest;
    }

    /**
     * Sorts a collection of elements that implement the {@link ILocation} interface in ascending order based on their squared distance
     * to a specified location.
     *
     * @param location the reference location used to calculate distances; must not be null
     * @param elements the collection of elements to sort; must not be null
     * @param <T>      the type of elements in the collection, which must extend {@link ILocation}
     *
     * @return a sorted list of elements in ascending order of their squared distance to the given location
     */
    public static <T extends ILocation> @Unmodifiable @NonNull List<T> sort(@NonNull Location location,
                                                                            @NonNull Collection<T> elements) {
        World world = location.getWorld();
        return elements.stream()
                .sorted(comparingDouble(t -> location.distanceSquared(t.getLocation(world))))
                .toList();
    }

    /**
     * Sorts a collection of elements that implement the {@link ILocation} interface in ascending order based on their distance to a
     * specified location, and returns a {@link LinkedHashMap} where the keys are the elements and the values are their respective
     * distances.
     *
     * @param location the reference location used to calculate distances; must not be null
     * @param elements the collection of elements to sort, which must implement {@link ILocation}; must not be null
     * @param <T>      the type of elements in the collection, which must extend {@link ILocation}
     *
     * @return a {@link LinkedHashMap} of the elements sorted in ascending order of their distance to the given location, with the keys
     *         being the elements and the values being their distances
     */
    public static <T extends ILocation> LinkedHashMap<T, Double> sortWithDistance(@NonNull Location location,
                                                                                  @NonNull Collection<T> elements) {
        World world = location.getWorld();
        return elements.stream()
                .collect(toMap(t -> t, t -> location.distance(t.getLocation(world)))).entrySet().stream()
                .sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}

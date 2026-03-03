package de.rettichlp.therettingtonconcierge.inventory.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.inventory.components.CraftCustomModelDataComponent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static net.kyori.adventure.text.Component.text;
import static org.bukkit.Bukkit.createProfile;
import static org.bukkit.Material.STRUCTURE_VOID;

@Data
public class Item {

    /**
     * A predefined {@link ItemStack} instance representing a transparent item.
     * <p>
     * This {@link ItemStack} is created using a material of type {@code STRUCTURE_VOID}, with a custom model data identifier set to
     * {@code "invisible"} for visual customization. Additionally, the tooltip for this item is hidden to provide a clean display
     * without any additional item information.
     * <p>
     * This constant is immutable and can be used as is wherever a transparent item representation via an {@link ItemStack} is
     * required.
     */
    public static final ItemStack TRANSPARENT_ITEM_STACK = Item.builder(STRUCTURE_VOID)
            .customModelData("gui.invisible")
            .hideTooltip()
            .build();

    /**
     * Creates a new {@link Builder} instance initialized with an {@link ItemStack} created using the specified material and a default
     * amount of 1.
     *
     * @param material the material to use for creating the {@link ItemStack}; must not be null
     *
     * @return a new {@link Builder} instance for modifying the created {@link ItemStack}
     */
    @Contract("_ -> new")
    public static @NonNull Builder builder(Material material) {
        return builder(material, 1);
    }

    /**
     * Creates a new {@link Builder} instance initialized with an {@link ItemStack} created using the specified material and amount.
     *
     * @param material the material to use for creating the {@link ItemStack}; must not be null
     * @param amount   the amount of the material for the {@link ItemStack}; must be greater than zero
     *
     * @return a new {@link Builder} instance for modifying the created {@link ItemStack}
     */
    @Contract("_, _ -> new")
    public static @NonNull Builder builder(Material material, int amount) {
        return builder(new ItemStack(material, amount));
    }

    /**
     * Creates a new {@link Builder} instance initialized with a cloned copy of the provided {@link ItemStack}.
     *
     * @param stack the {@link ItemStack} to initialize the builder with; must not be null
     *
     * @return a new {@link Builder} instance for modifying the given {@link ItemStack}
     */
    @Contract("_ -> new")
    public static @NonNull Builder builder(@NonNull ItemStack stack) {
        return new Builder(stack.clone());
    }

    /**
     * Builder is a utility class designed to simplify the creation and customization of Minecraft items. It allows fluent modification
     * of various item properties, such as display name, lore, model data, color, damage, and more. The Builder operates on an
     * {@link ItemStack}, applying modifications through the underlying {@link ItemMeta} associated with the item.
     * <p>
     * This class supports method chaining for seamless customization of the item and provides both basic and advanced options, such as
     * custom skull textures, custom model data, and translation keys.
     */
    public static class Builder {

        private final ItemStack itemStack;

        /**
         * Constructs a new Builder instance with the specified {@link ItemStack}. Automatically hides item flags for the provided
         * item.
         *
         * @param stack the {@link ItemStack} to be used for this builder; must not be null
         */
        public Builder(ItemStack stack) {
            this.itemStack = stack;
            this.itemStack.editMeta(itemMeta -> itemMeta.addItemFlags(ItemFlag.values()));
        }

        /**
         * Sets the display name of the item represented by the builder.
         *
         * @param displayName the display name to set, represented as a {@link String}
         *
         * @return the current Builder instance for method chaining
         */
        public Builder displayName(String displayName) {
            this.itemStack.editMeta(itemMeta -> itemMeta.displayName(text(displayName)));
            return this;
        }

        /**
         * Sets the display name of the item represented by the builder.
         *
         * @param displayName the display name to set, represented as a {@link Component}
         *
         * @return the current Builder instance for method chaining
         */
        public Builder displayName(Component displayName) {
            this.itemStack.editMeta(itemMeta -> itemMeta.displayName(displayName));
            return this;
        }

        /**
         * Sets the lore for the item represented by the builder using an array of {@link Component}. Each {@link Component} represents
         * a line of lore.
         *
         * @param lore the array of {@link Component} instances representing the lore lines; must not be null
         *
         * @return the current Builder instance for method chaining
         */
        public Builder lore(Component... lore) {
            return lore(asList(lore));
        }

        /**
         * Sets the lore for the item represented by the builder using a {@link List} of {@link Component}. Each {@link Component} in
         * the list represents a line of lore.
         *
         * @param lore a {@link List} of {@link Component} instances representing the lore lines; must not be null
         *
         * @return the current Builder instance for method chaining
         */
        public Builder lore(List<Component> lore) {
            this.itemStack.editMeta(itemMeta -> itemMeta.lore(lore));
            return this;
        }

        /**
         * Sets the custom model data for the item represented by the builder. The custom model data can be either a number or a
         * string. Based on the type of the provided data, it is processed and stored in the corresponding format for further use in
         * the item customisation.
         *
         * @param customModelData the custom model data to be set; must not be null. It can be of type {@link Number} or
         *                        {@link String}.
         *
         * @return the current Builder instance for method chaining.
         *
         * @throws IllegalStateException if the provided custom model data is of an unsupported type.
         */
        public Builder customModelData(@NonNull Object customModelData) {
            Map<String, Object> customModelDataMap = new HashMap<>();

            switch (customModelData) {
                case Number number -> customModelDataMap.put("floats", List.of(number));
                case String string -> customModelDataMap.put("strings", List.of(string));
                default -> throw new IllegalStateException("Unexpected value: " + customModelData.getClass());
            }

            this.itemStack.editMeta(itemMeta -> itemMeta.setCustomModelDataComponent(new CraftCustomModelDataComponent(customModelDataMap)));
            return this;
        }

        /**
         * Sets the color of the leather armor represented by the builder. This operation applies only to items that are instances of
         * {@link LeatherArmorMeta}.
         *
         * @param color the {@link Color} to set for the leather armor; must not be null
         *
         * @return the current Builder instance for method chaining
         */
        public Builder color(Color color) {
            this.itemStack.editMeta(itemMeta -> {
                if (itemMeta instanceof LeatherArmorMeta leatherArmorMeta) {
                    leatherArmorMeta.setColor(color);
                }
            });

            return this;
        }

        /**
         * Sets the damage value for the item represented by this builder. This operation only applies to items that implement the
         * {@link Damageable} interface. The damage value determines the durability state of the item.
         *
         * @param damage the damage value to set for the item; must be a non-negative integer
         *
         * @return the current Builder instance for method chaining
         */
        public Builder damage(int damage) {
            this.itemStack.editMeta(itemMeta -> {
                if (itemMeta instanceof Damageable damageable) {
                    damageable.setDamage(damage);
                }
            });

            return this;
        }

        /**
         * Sets the texture of the skull item represented by this builder. This operation applies only to items that are instances of
         * {@link SkullMeta}. The texture is set based on the provided {@link OfflinePlayer}'s profile.
         *
         * @param owner the {@link OfflinePlayer} whose profile is used to set the skull texture; must not be null
         *
         * @return the current Builder instance for method chaining
         */
        public Builder skullTexture(OfflinePlayer owner) {
            this.itemStack.editMeta(itemMeta -> {
                if (itemMeta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(owner);
                }
            });

            return this;
        }

        /**
         * Sets the texture of the skull item represented by this builder using the provided {@link PlayerProfile}. This operation
         * applies only to items that are instances of {@link SkullMeta}.
         *
         * @param profile the {@link PlayerProfile} to use for setting the skull texture; must not be null
         *
         * @return the current Builder instance for method chaining
         */
        public Builder skullTexture(PlayerProfile profile) {
            this.itemStack.editMeta(itemMeta -> {
                if (itemMeta instanceof SkullMeta skullMeta) {
                    skullMeta.setPlayerProfile(profile);
                }
            });

            return this;
        }

        /**
         * Sets the texture of the skull item represented by this builder using a texture ID. This operation applies only to items that
         * are instances of {@link SkullMeta}. The texture is set based on the provided texture ID, which must correspond to a valid
         * Minecraft skin texture ID.
         *
         * @param textureID the texture ID to use for setting the skull texture; must not be null and should represent a valid texture
         *                  from the Minecraft textures API
         *
         * @return the current Builder instance for method chaining
         *
         * @throws IllegalArgumentException if the provided texture ID is invalid or cannot be converted to a valid URL
         */
        public Builder skullTexture(String textureID) {
            this.itemStack.editMeta(itemMeta -> {
                if (itemMeta instanceof SkullMeta skullMeta) {
                    PlayerProfile profile = createProfile(randomUUID());
                    PlayerTextures textures = profile.getTextures();
                    try {
                        textures.setSkin(new URI("https://textures.minecraft.net/texture/%s".formatted(textureID)).toURL());
                    } catch (MalformedURLException | URISyntaxException e) {
                        throw new IllegalArgumentException("Invalid texture ID: " + textureID);
                    }

                    profile.setTextures(textures);
                    skullMeta.setPlayerProfile(profile);
                }
            });

            return this;
        }

        /**
         * Hides the tooltip for the item represented by this builder, including the name.
         *
         * @return the current Builder instance for method chaining
         */
        public Builder hideTooltip() {
            this.itemStack.editMeta(itemMeta -> itemMeta.setHideTooltip(true));
            return this;
        }

        /**
         * Enables the enchantment glint for the item represented by the builder.
         *
         * @return the current Builder instance for method chaining
         */
        public Builder glint() {
            return glint(true);
        }

        /**
         * Sets the enchantment glint override state for the item represented by the builder.
         *
         * @param state the desired glint state; {@code true} enables the glint, {@code false} disables it
         *
         * @return the current Builder instance for method chaining
         */
        public Builder glint(boolean state) {
            this.itemStack.editMeta(itemMeta -> itemMeta.setEnchantmentGlintOverride(state));
            return this;
        }

        /**
         * Constructs and retrieves the final {@link ItemStack} configured by this builder.
         *
         * @return the constructed {@link ItemStack} instance, reflecting all settings applied via the builder.
         */
        public ItemStack build() {
            return this.itemStack;
        }
    }
}

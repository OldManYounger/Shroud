package net.oldmanyounger.shroud.ritual.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.Registries;

/**
 * Immutable ritual recipe model loaded from datapack JSON.
 *
 * <p>This model stores unordered item requirements, entity-count pedestal requirements,
 * and a simple item output payload for the ritual system. It intentionally does not execute
 * ritual logic and is purely recipe data.
 *
 * <p>In the broader context of the project, this class provides the canonical data contract
 * between JSON-authored ritual definitions and runtime ritual validation code.
 */
public record RitualRecipe(
        ResourceLocation id,
        java.util.List<ItemRequirement> itemRequirements,
        java.util.List<MobRequirement> mobRequirements,
        ItemStack output
) {

    /**
     * One item-side requirement entry for a ritual recipe.
     *
     * <p>A requirement can target either an exact item id or an item tag, with a required count.
     * Matching is handled later by ritual validation logic.
     *
     * <p>In the broader context of the project, this class standardizes how reliquary-side
     * ingredients are described in data.
     */
    public record ItemRequirement(
            @javax.annotation.Nullable Item item,
            @javax.annotation.Nullable TagKey<Item> tag,
            int count
    ) {
        // Returns true when this requirement is exact-item based
        public boolean isItemRequirement() {
            return item != null;
        }

        // Returns true when this requirement is tag based
        public boolean isTagRequirement() {
            return tag != null;
        }

        // Returns true if the given stack satisfies this requirement selector
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) return false;
            if (item != null) return stack.is(item);
            if (tag != null) return stack.is(tag);
            return false;
        }
    }

    /**
     * One mob-side requirement entry for a ritual recipe.
     *
     * <p>This defines an exact entity type and count requirement. Count semantics match your
     * clarified rule where each required mob instance must be on its own pedestal.
     *
     * <p>In the broader context of the project, this class standardizes how pedestal-side
     * mob requirements are described in data.
     */
    public record MobRequirement(
            EntityType<?> entityType,
            int count
    ) {

    }
}
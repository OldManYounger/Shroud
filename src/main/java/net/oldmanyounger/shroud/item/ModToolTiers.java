package net.oldmanyounger.shroud.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/**
 * Declares custom tool tier definitions used by Shroud.
 *
 * <p>This class provides shared tool tier constants that define mining level
 * compatibility, durability, speed, attack bonus, enchantability, and repair ingredients.
 *
 * <p>In the broader context of the project, this class is part of Shroud's item
 * balance layer that standardizes how custom toolsets interact with vanilla mining
 * progression and equipment tuning.
 */
public class ModToolTiers {

    // Eventide tool tier used by all Eventide tool item registrations
    public static final Tier EVENTIDE = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2300, 10.0f, 4.0f, 18,
            () -> Ingredient.of(ModItems.EVENTIDE_INGOT.get())
    );
}
package net.oldmanyounger.shroud.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {

    public static final Tier EVENTIDE = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2300, 10.0f, 4.0f, 18,
                () -> Ingredient.of(ModItems.EVENTIDE_INGOT.get())
    );
}
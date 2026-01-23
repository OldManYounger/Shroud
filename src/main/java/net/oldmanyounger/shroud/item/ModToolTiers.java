package net.oldmanyounger.shroud.item;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.oldmanyounger.shroud.util.ModTags;

public class ModToolTiers {

    public static final Tier EVENTIDE = new SimpleTier(
            ModTags.Blocks.INCORRECT_FOR_EVENTIDE_TOOL,
            1400,
            4f,
            3f,
            28,
            () -> Ingredient.of(ModItems.EVENTIDE_INGOT.get())
    );
}
package net.oldmanyounger.shroud.compat.jei;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI-facing immutable display payload for one ritual recipe.
 *
 * <p>This model decouples JEI rendering data from runtime ritual execution types so
 * UI composition remains stable even if execution internals evolve.
 *
 * <p>In the broader context of the project, this class is part of the compatibility
 * adapter layer that exposes ritual crafting to optional client integrations.
 */
public record JeiRitualDisplay(
        ResourceLocation id,
        List<ItemStack> itemInputs,
        List<String> mobRequirementLines,
        float mobDamagePerRequiredMob,
        ItemStack output
) {

}
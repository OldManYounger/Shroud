package net.oldmanyounger.shroud.ritual.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Minimal bridge recipe implementation for the `shroud:ritual` recipe id.
 *
 * <p>This recipe exists solely to provide a valid registered recipe class for
 * datapack and KubeJS type resolution, and does not participate in vanilla
 * crafting-table matching or assembly.
 *
 * <p>In the broader context of the project, this class is a compatibility shim
 * between Shroud ritual definitions and external recipe tooling expectations.
 */
public class RitualRegisteredRecipe implements Recipe<RecipeInput> {

    // Returns false because ritual matching is handled by Shroud systems
    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    // Returns empty output because this bridge recipe does not assemble items
    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    // Returns false because this is not a grid recipe
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    // Returns empty display output for this bridge recipe
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    // Returns the registered ritual serializer
    @Override
    public RecipeSerializer<?> getSerializer() {
        return RitualRecipeRegistries.RITUAL_SERIALIZER.get();
    }

    // Returns the registered ritual recipe type
    @Override
    public RecipeType<?> getType() {
        return RitualRecipeRegistries.RITUAL_TYPE.get();
    }

    // Returns true so this recipe is treated as special
    @Override
    public boolean isSpecial() {
        return true;
    }
}
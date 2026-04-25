package net.oldmanyounger.shroud.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipeRegistries;

import java.util.List;

/**
 * JEI plugin entrypoint for Shroud ritual crafting integration.
 *
 * <p>This plugin registers ritual recipe category visuals, ritual recipe data, and
 * catalyst linkage while keeping all JEI API references fully isolated to this package.
 *
 * <p>In the broader context of the project, this class enables optional client-side
 * discoverability of ritual systems without adding mandatory dependencies.
 */
@JeiPlugin
public final class ShroudJeiPlugin implements IModPlugin {

    // ==================================
    //  FIELDS
    // ==================================

    // Plugin uid resource id
    private static final ResourceLocation PLUGIN_UID =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "jei_plugin");

    // Returns unique JEI plugin uid
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    // Registers ritual category
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JeiRitualRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    // Registers ritual recipes from recipe manager ritual type entries
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            registration.addRecipes(JeiRitualRecipeCategory.RECIPE_TYPE, List.of());
            return;
        }

        List<JeiRitualDisplay> displays = minecraft.level.getRecipeManager()
                .getAllRecipesFor(RitualRecipeRegistries.RITUAL_TYPE.get())
                .stream()
                .map(holder -> holder.value().toRuntime(holder.id()))
                .map(this::mapRecipe)
                .toList();

        registration.addRecipes(JeiRitualRecipeCategory.RECIPE_TYPE, displays);
    }

    // Registers ritual catalysts
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                ModBlocks.CORRUPTED_RELIQUARY.get().asItem().getDefaultInstance(),
                JeiRitualRecipeCategory.RECIPE_TYPE
        );
    }

    // Maps one ritual recipe model into JEI display payload
    private JeiRitualDisplay mapRecipe(RitualRecipe recipe) {
        return JeiRitualDisplayMapper.map(recipe);
    }
}
package net.oldmanyounger.shroud.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipeManager;

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

    // Registers ritual recipes
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<JeiRitualDisplay> displays = RitualRecipeManager.INSTANCE.getAll()
                .stream()
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
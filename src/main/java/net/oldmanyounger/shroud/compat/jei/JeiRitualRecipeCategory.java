package net.oldmanyounger.shroud.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;

/**
 * JEI recipe category for rendering Shroud ritual recipes.
 *
 * <p>This class defines JEI slot layout and extra text rendering for ritual mob requirements
 * and mob damage metadata.
 *
 * <p>In the broader context of the project, this class provides the optional client UI bridge
 * that exposes ritual datapack content to players without touching ritual runtime logic.
 */
@SuppressWarnings("removal")
public final class JeiRitualRecipeCategory implements IRecipeCategory<JeiRitualDisplay> {

    // ==================================
    //  FIELDS
    // ==================================

    // JEI recipe type for ritual crafting
    public static final RecipeType<JeiRitualDisplay> RECIPE_TYPE =
            RecipeType.create(Shroud.MOD_ID, "ritual_crafting", JeiRitualDisplay.class);

    // Category width
    private static final int WIDTH = 176;

    // Category height
    private static final int HEIGHT = 96;

    // Primary readable text color
    private static final int TEXT_COLOR_PRIMARY = 0xFFFFFF;

    // Secondary readable text color
    private static final int TEXT_COLOR_SECONDARY = 0xF2F2F2;

    // Cached background drawable
    private final IDrawable background;

    // Cached icon drawable
    private final IDrawable icon;

    // Creates the ritual category
    public JeiRitualRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(ModBlocks.CORRUPTED_RELIQUARY.get().asItem().getDefaultInstance());
    }

    // Returns the JEI recipe type
    @Override
    public RecipeType<JeiRitualDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    // Returns the category title
    @Override
    public Component getTitle() {
        return Component.translatable("jei.shroud.ritual_crafting");
    }

    // Returns the category background
    @Override
    public IDrawable getBackground() {
        return background;
    }

    // Returns the category icon
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    // Defines slot layout for ritual inputs and output
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JeiRitualDisplay recipe, IFocusGroup focuses) {
        int startX = 8;
        int startY = 8;

        int column = 0;
        int row = 0;

        for (var input : recipe.itemInputs()) {
            int x = startX + (column * 18);
            int y = startY + (row * 18);

            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .setStandardSlotBackground()
                    .addItemStack(input);

            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 140, 26)
                .setStandardSlotBackground()
                .addItemStack(recipe.output());
    }

    // Draws non-slot ritual metadata text
    @Override
    public void draw(JeiRitualDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;

        int textX = 8;
        int textY = 54;

        guiGraphics.drawString(font, Component.translatable("jei.shroud.ritual_mobs"), textX, textY, TEXT_COLOR_PRIMARY, false);
        textY += 10;

        for (String line : recipe.mobRequirementLines()) {
            guiGraphics.drawString(font, line, textX, textY, TEXT_COLOR_PRIMARY, false);
            textY += 10;
            if (textY > 86) {
                break;
            }
        }

        guiGraphics.drawString(
                font,
                Component.translatable("jei.shroud.ritual_damage", recipe.mobDamagePerRequiredMob()),
                textX,
                textY + 2,
                TEXT_COLOR_SECONDARY,
                false
        );
    }
}
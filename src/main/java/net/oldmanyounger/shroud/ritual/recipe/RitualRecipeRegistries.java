package net.oldmanyounger.shroud.ritual.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/**
 * Registers ritual recipe type and serializer entries in NeoForge registries.
 *
 * <p>This class declares the `shroud:ritual` recipe type and serializer so
 * datapack systems and integration mods such as KubeJS can resolve the ritual
 * recipe id as a valid registered recipe.
 *
 * <p>In the broader context of the project, this class is the registry bridge
 * that allows Shroud ritual data to interoperate with standard recipe-loading
 * pipelines while ritual execution remains handled by Shroud runtime systems.
 */
public class RitualRecipeRegistries {

    // ==================================
    //  FIELDS
    // ==================================

    // Deferred register for recipe types
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Shroud.MOD_ID);

    // Deferred register for recipe serializers
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Shroud.MOD_ID);

    // Ritual recipe type handle
    public static final DeferredHolder<RecipeType<?>, RecipeType<RitualRegisteredRecipe>> RITUAL_TYPE =
            RECIPE_TYPES.register("ritual", () -> new RecipeType<RitualRegisteredRecipe>() {
                @Override
                public String toString() {
                    return Shroud.MOD_ID + ":ritual";
                }
            });

    // Ritual recipe serializer handle
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RitualRegisteredRecipe>> RITUAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("ritual", RitualRegisteredRecipeSerializer::new);

    // Registers all ritual recipe registry entries
    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
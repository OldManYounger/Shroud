package net.oldmanyounger.shroud.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Generates ritual recipe JSON definitions for Shroud ritual crafting.
 *
 * <p>This provider emits custom ritual recipe data under the datapack data tree so
 * ritual definitions can be authored in code-driven datagen workflows alongside
 * the rest of the project's generated assets.
 *
 * <p>In the broader context of the project, this class keeps ritual recipe content
 * aligned with the mod's datagen pipeline and reduces manual JSON drift over time.
 */
public class ModRitualRecipeProvider implements DataProvider {

    // ==================================
    //  FIELDS
    // ==================================

    // Output path resolver for ritual recipe JSON files
    private final PackOutput.PathProvider ritualRecipePathProvider;

    // Creates the ritual recipe provider
    public ModRitualRecipeProvider(PackOutput output) {
        this.ritualRecipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe/ritual");
    }

    // Writes all ritual recipe JSON files
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject totemRecipe = buildTotemOfLastBreathRecipe();
        Path totemRecipePath = ritualRecipePathProvider.json(
                ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "totem_of_last_breath_ritual")
        );

        return DataProvider.saveStable(cachedOutput, totemRecipe, totemRecipePath);
    }

    // Returns a display name for this provider
    @Override
    public String getName() {
        return "Shroud Ritual Recipe Provider";
    }

    // Builds the test v1 ritual recipe for Totem of Last Breath
    private JsonObject buildTotemOfLastBreathRecipe() {
        JsonObject root = new JsonObject();
        root.addProperty("type", "shroud:ritual");

        JsonArray items = new JsonArray();
        items.add(itemRequirement("minecraft:echo_shard", 2));
        items.add(itemRequirement("shroud:sculk_pearl", 1));
        items.add(itemRequirement("shroud:gloomstone_dust", 4));
        items.add(itemRequirement("minecraft:totem_of_undying", 1));
        root.add("items", items);

        JsonArray mobs = new JsonArray();
        mobs.add(mobRequirement("shroud:living_sculk", 4));
        root.add("mobs", mobs);

        root.addProperty("mob_damage", 4.0F);
        root.addProperty("duration_seconds", 8);

        JsonObject output = new JsonObject();
        output.addProperty("item", "shroud:totem_of_last_breath");
        output.addProperty("count", 1);
        root.add("output", output);

        return root;
    }

    // Creates an item requirement object for ritual recipe JSON
    private JsonObject itemRequirement(String itemId, int count) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", itemId);
        obj.addProperty("count", count);
        return obj;
    }

    // Creates a mob requirement object for ritual recipe JSON
    private JsonObject mobRequirement(String entityId, int count) {
        JsonObject obj = new JsonObject();
        obj.addProperty("entity", entityId);
        obj.addProperty("count", count);
        return obj;
    }
}
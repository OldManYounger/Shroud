package net.oldmanyounger.shroud.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.ritual.recipe.CorruptedReliquaryRecipeJsonFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generates base corrupted reliquary recipe JSON files for the Shroud datapack output.
 *
 * <p>This provider emits canonical ritual recipe data under the dedicated ritual
 * recipe folder, allowing base mod recipes to be maintained in Java while still
 * producing datapack-readable JSON resources.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * data-generation layer that keeps built-in ritual content aligned with the
 * runtime JSON loader and external datapack extension workflows.
 */
public class ModCorruptedReliquaryRecipeProvider implements DataProvider {

    // Path provider targeting ritual recipe json output folder
    private final PackOutput.PathProvider pathProvider;

    // Creates provider with datapack output target
    public ModCorruptedReliquaryRecipeProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(
                PackOutput.Target.DATA_PACK,
                "shroud_reliquary_recipes"
        );
    }

    // Writes generated ritual recipe files to output
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> writes = new ArrayList<>();

        // Example recipe for initial validation and integration testing
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "example_last_breath_ritual");
        JsonObject json = CorruptedReliquaryRecipeJsonFactory.exactEntityRecipe(
                List.of(
                        new CorruptedReliquaryRecipeJsonFactory.ItemInput(
                                ResourceLocation.withDefaultNamespace("echo_shard"),
                                2
                        ),
                        new CorruptedReliquaryRecipeJsonFactory.ItemInput(
                                ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "sculk_pearl"),
                                1
                        )
                ),
                ResourceLocation.withDefaultNamespace("zombie"), 4.0F,
                ResourceLocation.withDefaultNamespace("skeleton"), 4.0F,
                ResourceLocation.withDefaultNamespace("spider"), 2.0F,
                ResourceLocation.withDefaultNamespace("creeper"), 6.0F,
                new CorruptedReliquaryRecipeJsonFactory.ItemOutput(
                        ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "totem_of_last_breath"),
                        1
                )
        );

        Path outPath = this.pathProvider.json(recipeId);
        writes.add(DataProvider.saveStable(cachedOutput, json, outPath));

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    // Returns provider name shown in datagen logs
    @Override
    public String getName() {
        return "Shroud Corrupted Reliquary Recipes";
    }
}
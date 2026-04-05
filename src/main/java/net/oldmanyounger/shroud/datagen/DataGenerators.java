package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.oldmanyounger.shroud.Shroud;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Entry point for Shroud's data generation pipeline.
 *
 * <p>This class listens for NeoForge's gather-data event and wires together all
 * of the individual datagen providers responsible for producing recipes, block
 * states, item models, loot tables, tags, and built-in datapack content. Rather
 * than placing that setup logic across multiple unrelated classes, it keeps the
 * mod's entire datagen registration flow centralized and easy to expand.
 *
 * <p>In the broader context of the project, this class acts as the orchestration
 * layer for generating the JSON assets that back much of Shroud's content,
 * helping keep runtime resources consistent with the Java registrations.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public class DataGenerators {

    // Handles the gather-data event and attaches all active client/server providers
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        // Main generator used to register all providers
        DataGenerator generator = event.getGenerator();

        // Destination pack output for generated files
        PackOutput packOutput = generator.getPackOutput();

        // Helper used to validate references to existing generated or static assets
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Registry lookup future shared by multiple datagen providers
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Block loot table generation
        generator.addProvider(event.includeServer(), new LootTableProvider(
                packOutput,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider
        ));

        // Recipe generation
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));

        // Block tag generation
        BlockTagsProvider blockTagsProvider =
                new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // Item tag generation, using block tag contents where needed
        generator.addProvider(event.includeServer(),
                new ModItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        // Item model generation
        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));

        // Blockstate and block model generation
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(packOutput, existingFileHelper));

        // Optional data map generation hook, left disabled unless needed later
        //        generator.addProvider(event.includeServer(),
        //                new ModDataMapProvider(packOutput, lookupProvider));

        // Built-in datapack generation for worldgen and trim content
        generator.addProvider(event.includeServer(), new ModDatapackProvider(packOutput, lookupProvider));

        // Ritual recipe generation
        generator.addProvider(event.includeServer(), new ModRitualRecipeProvider(packOutput));
    }
}
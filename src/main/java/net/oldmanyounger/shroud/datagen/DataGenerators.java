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

/** Registers all data-generation providers used by the Shroud mod */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public class DataGenerators {

    /** Handles the GatherDataEvent and attaches all server and client datagen providers */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {

        // Main generator instance that coordinates all datagen providers
        DataGenerator generator = event.getGenerator();

        // Output handler responsible for writing pack files
        PackOutput packOutput = generator.getPackOutput();

        // Helper used for validating references to existing models/textures
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Registry lookup provider used by recipes, worldgen, tags, etc.
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Loot table generation provider
        generator.addProvider(event.includeServer(), new LootTableProvider(packOutput, Collections.emptySet(), List.of(
                new LootTableProvider.SubProviderEntry(ModBlockLootTableProvider::new, LootContextParamSets.BLOCK)), lookupProvider));

        // Recipe generation provider
        generator.addProvider(event.includeServer(),
                new ModRecipeProvider(packOutput, lookupProvider));

        // Block tags generation provider
        BlockTagsProvider blockTagsProvider =
                new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);

        // Item tags generation provider
        generator.addProvider(event.includeServer(),
                new ModItemTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        // Item model generation provider
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(packOutput, existingFileHelper));

        // Blockstate + block model generation provider
        generator.addProvider(event.includeClient(),
                new ModBlockStateProvider(packOutput, existingFileHelper));

        // Optional: datamap provider (disabled unless needed)
//        generator.addProvider(event.includeServer(),
//                new ModDataMapProvider(packOutput, lookupProvider));

        // Datapack provider (dimensions, biomes, and other JSON worldgen)
        generator.addProvider(event.includeServer(),
                new ModDatapackProvider(packOutput, lookupProvider));
    }
}

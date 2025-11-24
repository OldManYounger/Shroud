package net.oldmanyounger.shroud.datagen;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.worldgen.ModConfiguredFeatures;
import net.oldmanyounger.shroud.worldgen.ModPlacedFeatures;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Registers built-in datapack worldgen entries such as configured and placed features */
public class ModDatapackProvider extends DatapackBuiltinEntriesProvider {

    /** Registry builder that wires configured and placed features into the built-in datapack */
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap);

    /** Creates the datapack provider bound to the Shroud mod ID and registry builder */
    public ModDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Shroud.MOD_ID));
    }
}

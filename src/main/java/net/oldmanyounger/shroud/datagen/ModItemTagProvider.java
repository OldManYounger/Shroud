package net.oldmanyounger.shroud.datagen;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** Generates all item tag assignments for Shroud items */
public class ModItemTagProvider extends ItemTagsProvider {

    /** Creates the item tag provider linked to the Shroud namespace */
    public ModItemTagProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags,
                              @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Shroud.MOD_ID, existingFileHelper);
    }

    /** Registers all item tag entries for the Sculk wood set */
    @Override
    protected void addTags(HolderLookup.Provider provider) {

        // Registers Sculk log family under burnable-log item tags
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.SCULK_LOG.get().asItem())
                .add(ModBlocks.SCULK_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_SCULK_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_SCULK_WOOD.get().asItem());

        // Registers Sculk planks under the standard plank item tag
        tag(ItemTags.PLANKS)
                .add(ModBlocks.SCULK_PLANKS.asItem());
    }
}

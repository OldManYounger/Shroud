package net.oldmanyounger.shroud.datagen;

import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.util.ModTags;
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

        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.EVENTIDE_INGOT.get())
                .add(ModItems.RAW_EVENTIDE.get());

        // Sword and tool tags
        tag(ItemTags.SWORDS)
                .add(ModItems.EVENTIDE_SWORD.get());
        tag(ItemTags.PICKAXES)
                .add(ModItems.EVENTIDE_PICKAXE.get());
        tag(ItemTags.SHOVELS)
                .add(ModItems.EVENTIDE_SHOVEL.get());
        tag(ItemTags.AXES)
                .add(ModItems.EVENTIDE_AXE.get());
        tag(ItemTags.HOES)
                .add(ModItems.EVENTIDE_HOE.get());

        // Trimmable armor tags
        this.tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.EVENTIDE_HELMET.get())
                .add(ModItems.EVENTIDE_CHESTPLATE.get())
                .add(ModItems.EVENTIDE_LEGGINGS.get())
                .add(ModItems.EVENTIDE_BOOTS.get());

        // Trim tags
        this.tag(ItemTags.TRIM_MATERIALS)
                .add(ModItems.EVENTIDE_INGOT.get());

        this.tag(ItemTags.TRIM_TEMPLATES)
                .add(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());


        // Registers Sculk log family under burnable-log item tags
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.SCULK_LOG.get().asItem())
                .add(ModBlocks.SCULK_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_SCULK_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_SCULK_WOOD.get().asItem());

        // Registers Sculk planks under the standard plank item tag
        tag(ItemTags.PLANKS)
                .add(ModBlocks.SCULK_PLANKS.asItem());

        // Registers Umber log family under burnable-log item tags
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.UMBER_LOG.get().asItem())
                .add(ModBlocks.UMBER_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_UMBER_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_UMBER_WOOD.get().asItem());

        // Registers Umber planks under the standard plank item tag
        tag(ItemTags.PLANKS)
                .add(ModBlocks.UMBER_PLANKS.asItem());

    }
}

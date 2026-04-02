package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.item.ModItems;
import net.oldmanyounger.shroud.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Generates item tag assignments for Shroud items.
 *
 * <p>This provider places Shroud items into vanilla and custom item tags so
 * they interact correctly with crafting systems, tool classification, armor trim
 * systems, and material grouping. It also mirrors the mod's custom wood sets
 * into the expected vanilla item tags for log and plank behavior.
 *
 * <p>In the broader context of the project, this class helps ensure Shroud items
 * participate naturally in Minecraft's tag-driven systems and remain consistent
 * with their corresponding block and equipment content.
 */
public class ModItemTagProvider extends ItemTagsProvider {

    // Creates the item tag provider for the Shroud namespace
    public ModItemTagProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookupProvider,
                              CompletableFuture<TagLookup<Block>> blockTags,
                              @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Shroud.MOD_ID, existingFileHelper);
    }

    // Adds all Shroud item tag entries
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Custom transformable items
        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
                .add(ModItems.EVENTIDE_INGOT.get())
                .add(ModItems.RAW_EVENTIDE.get());

        // Tool categories
        tag(ItemTags.SWORDS).add(ModItems.EVENTIDE_SWORD.get());
        tag(ItemTags.PICKAXES).add(ModItems.EVENTIDE_PICKAXE.get());
        tag(ItemTags.SHOVELS).add(ModItems.EVENTIDE_SHOVEL.get());
        tag(ItemTags.AXES).add(ModItems.EVENTIDE_AXE.get());
        tag(ItemTags.HOES).add(ModItems.EVENTIDE_HOE.get());

        // Armor trim participation
        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.EVENTIDE_HELMET.get())
                .add(ModItems.EVENTIDE_CHESTPLATE.get())
                .add(ModItems.EVENTIDE_LEGGINGS.get())
                .add(ModItems.EVENTIDE_BOOTS.get());

        tag(ItemTags.TRIM_MATERIALS)
                .add(ModItems.EVENTIDE_INGOT.get());

        tag(ItemTags.TRIM_TEMPLATES)
                .add(ModItems.EVENTIDE_SMITHING_TEMPLATE.get());

        // Virelith wood family
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.VIRELITH_LOG.get().asItem())
                .add(ModBlocks.VIRELITH_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_VIRELITH_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_VIRELITH_WOOD.get().asItem());

        tag(ItemTags.PLANKS)
                .add(ModBlocks.VIRELITH_PLANKS.asItem());

        // Umber wood family
        tag(ItemTags.LOGS_THAT_BURN)
                .add(ModBlocks.UMBER_LOG.get().asItem())
                .add(ModBlocks.UMBER_WOOD.get().asItem())
                .add(ModBlocks.STRIPPED_UMBER_LOG.get().asItem())
                .add(ModBlocks.STRIPPED_UMBER_WOOD.get().asItem());

        tag(ItemTags.PLANKS)
                .add(ModBlocks.UMBER_PLANKS.asItem());
    }
}
package net.oldmanyounger.shroud.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.ModBlocks;
import net.oldmanyounger.shroud.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** Generates all block tag assignments for Shroud blocks */
public class ModBlockTagProvider extends BlockTagsProvider {

    /** Creates the tag provider for the Shroud namespace */
    public ModBlockTagProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider,
                               @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Shroud.MOD_ID, existingFileHelper);
    }

    /** Registers all block tag entries for the Sculk wood set and related blocks */
    @Override
    protected void addTags(HolderLookup.Provider provider) {

        // Sculk grass tags
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.SCULK_GRASS.get());
        tag(BlockTags.DIRT)
                .add(ModBlocks.SCULK_GRASS.get());
        tag(BlockTags.REPLACEABLE_BY_TREES)
                .add(ModBlocks.SCULK_GRASS.get());
        tag(BlockTags.VALID_SPAWN)
                .add(ModBlocks.SCULK_GRASS.get());

        // Sculk bulb tags

        // Eventide ore + block are mineable with pickaxe
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.EVENTIDE_BLOCK.get())
                .add(ModBlocks.EVENTIDE_ORE.get())
                .add(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get());

        // Eventide ore requires an iron-tier tool (adjust later if you add an Eventide tool tier)
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.EVENTIDE_ORE.get())
                .add(ModBlocks.EVENTIDE_DEEPSLATE_ORE.get());

        // Optional: if you are using your custom "needs_eventide_tool" tag
        tag(ModTags.Blocks.NEEDS_EVENTIDE_TOOL)
                .addTag(BlockTags.NEEDS_IRON_TOOL);

        // Optional: if you are using your custom "incorrect_for_eventide_tool" tag
        tag(ModTags.Blocks.INCORRECT_FOR_EVENTIDE_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .remove(ModTags.Blocks.NEEDS_EVENTIDE_TOOL);

        // Registers all Sculk wood-set blocks as axe-mineable wood-like blocks
        wooden(
                ModBlocks.SCULK_LOG.get(),
                ModBlocks.SCULK_WOOD.get(),
                ModBlocks.STRIPPED_SCULK_LOG.get(),
                ModBlocks.STRIPPED_SCULK_WOOD.get(),
                ModBlocks.SCULK_PLANKS.get(),
                ModBlocks.SCULK_STAIRS.get(),
                ModBlocks.SCULK_SLAB.get(),
                ModBlocks.SCULK_FENCE.get(),
                ModBlocks.SCULK_FENCE_GATE.get(),
                ModBlocks.SCULK_WALL.get(),
                ModBlocks.SCULK_DOOR.get(),
                ModBlocks.SCULK_TRAPDOOR.get(),
                ModBlocks.SCULK_BUTTON.get(),
                ModBlocks.SCULK_PRESSURE_PLATE.get()
        );

        // Defines the Sculk log + wood family under standard log tags
        tag(BlockTags.LOGS)
                .add(ModBlocks.SCULK_LOG.get())
                .add(ModBlocks.SCULK_WOOD.get())
                .add(ModBlocks.STRIPPED_SCULK_LOG.get())
                .add(ModBlocks.STRIPPED_SCULK_WOOD.get());

        tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.SCULK_LOG.get())
                .add(ModBlocks.SCULK_WOOD.get())
                .add(ModBlocks.STRIPPED_SCULK_LOG.get())
                .add(ModBlocks.STRIPPED_SCULK_WOOD.get());

        // Registers Sculk planks to vanilla plank tags
        tag(BlockTags.PLANKS)
                .add(ModBlocks.SCULK_PLANKS.get());

        // Registers Sculk foliage and sapling
        tag(BlockTags.LEAVES)
                .add(ModBlocks.SCULK_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(ModBlocks.SCULK_SAPLING.get());

        // Registers Sculk wood-set shapes such as stairs, slabs, fences, etc.
        stairs(ModBlocks.SCULK_STAIRS.get());
        slab(ModBlocks.SCULK_SLAB.get());

        fence(ModBlocks.SCULK_FENCE.get());
        fenceGate(ModBlocks.SCULK_FENCE_GATE.get());
        wall(ModBlocks.SCULK_WALL.get());

        door(ModBlocks.SCULK_DOOR.get());
        trapdoor(ModBlocks.SCULK_TRAPDOOR.get());

        button(ModBlocks.SCULK_BUTTON.get());
        pressurePlate(ModBlocks.SCULK_PRESSURE_PLATE.get());

        // Registers all Umber wood-set blocks as axe-mineable wood-like blocks
        wooden(
                ModBlocks.UMBER_LOG.get(),
                ModBlocks.UMBER_WOOD.get(),
                ModBlocks.STRIPPED_UMBER_LOG.get(),
                ModBlocks.STRIPPED_UMBER_WOOD.get(),
                ModBlocks.UMBER_PLANKS.get(),
                ModBlocks.UMBER_STAIRS.get(),
                ModBlocks.UMBER_SLAB.get(),
                ModBlocks.UMBER_FENCE.get(),
                ModBlocks.UMBER_FENCE_GATE.get(),
                ModBlocks.UMBER_WALL.get(),
                ModBlocks.UMBER_DOOR.get(),
                ModBlocks.UMBER_TRAPDOOR.get(),
                ModBlocks.UMBER_BUTTON.get(),
                ModBlocks.UMBER_PRESSURE_PLATE.get()
        );

        // Defines the Umber log + wood family under standard log tags
        tag(BlockTags.LOGS)
                .add(ModBlocks.UMBER_LOG.get())
                .add(ModBlocks.UMBER_WOOD.get())
                .add(ModBlocks.STRIPPED_UMBER_LOG.get())
                .add(ModBlocks.STRIPPED_UMBER_WOOD.get());

        tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.UMBER_LOG.get())
                .add(ModBlocks.UMBER_WOOD.get())
                .add(ModBlocks.STRIPPED_UMBER_LOG.get())
                .add(ModBlocks.STRIPPED_UMBER_WOOD.get());

        // Registers Umber planks to vanilla plank tags
        tag(BlockTags.PLANKS)
                .add(ModBlocks.UMBER_PLANKS.get());

        // Registers Umber foliage and sapling
        tag(BlockTags.LEAVES)
                .add(ModBlocks.UMBER_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(ModBlocks.UMBER_SAPLING.get());

        // Registers Umber wood-set shapes such as stairs, slabs, fences, etc.
        stairs(ModBlocks.UMBER_STAIRS.get());
        slab(ModBlocks.UMBER_SLAB.get());

        fence(ModBlocks.UMBER_FENCE.get());
        fenceGate(ModBlocks.UMBER_FENCE_GATE.get());
        wall(ModBlocks.UMBER_WALL.get());

        door(ModBlocks.UMBER_DOOR.get());
        trapdoor(ModBlocks.UMBER_TRAPDOOR.get());

        button(ModBlocks.UMBER_BUTTON.get());
        pressurePlate(ModBlocks.UMBER_PRESSURE_PLATE.get());

    }

    /** Adds all provided blocks to the mineable-with-axe tag */
    private void wooden(Block... blocks) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(blocks);
    }

    /** Registers a block as a stair and wooden stair */
    private void stairs(Block block) {
        tag(BlockTags.STAIRS).add(block);
        tag(BlockTags.WOODEN_STAIRS).add(block);
    }

    /** Registers a block as a slab and wooden slab */
    private void slab(Block block) {
        tag(BlockTags.SLABS).add(block);
        tag(BlockTags.WOODEN_SLABS).add(block);
    }

    /** Registers a block as a fence and wooden fence */
    private void fence(Block block) {
        tag(BlockTags.FENCES).add(block);
        tag(BlockTags.WOODEN_FENCES).add(block);
    }

    /** Registers a block as a fence gate */
    private void fenceGate(Block block) {
        tag(BlockTags.FENCE_GATES).add(block);
    }

    /** Registers a block as a wall */
    private void wall(Block block) {
        tag(BlockTags.WALLS).add(block);
    }

    /** Registers a block as a door and wooden door */
    private void door(Block block) {
        tag(BlockTags.DOORS).add(block);
        tag(BlockTags.WOODEN_DOORS).add(block);
    }

    /** Registers a block as a trapdoor and wooden trapdoor */
    private void trapdoor(Block block) {
        tag(BlockTags.TRAPDOORS).add(block);
        tag(BlockTags.WOODEN_TRAPDOORS).add(block);
    }

    /** Registers a block as a button and wooden button */
    private void button(Block block) {
        tag(BlockTags.BUTTONS).add(block);
        tag(BlockTags.WOODEN_BUTTONS).add(block);
    }

    /** Registers a block as a pressure plate and wooden pressure plate */
    private void pressurePlate(Block block) {
        tag(BlockTags.PRESSURE_PLATES).add(block);
        tag(BlockTags.WOODEN_PRESSURE_PLATES).add(block);
    }
}

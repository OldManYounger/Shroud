package net.oldmanyounger.shroud.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.oldmanyounger.shroud.Shroud;

/**
 * Centralizes custom block and item tag keys used by Shroud.
 *
 * <p>This class provides grouped nested holders for block tags and item tags,
 * along with helper methods that build namespaced tag keys.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * data-contract layer that keeps tag identifiers consistent between runtime
 * checks, data packs, and generation systems.
 */
public class ModTags {

    // Block tag key container
    public static class Blocks {

        // Blocks that require Eventide-tier tools
        public static final TagKey<Block> NEEDS_EVENTIDE_TOOL = createTag("needs_eventide_tool");

        // Blocks that are incorrect for Eventide-tier tools
        public static final TagKey<Block> INCORRECT_FOR_EVENTIDE_TOOL = createTag("incorrect_for_eventide_tool");

        // Creates a namespaced block tag key
        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name));
        }
    }

    // Item tag key container
    public static class Items {

        // Items eligible for custom transformation logic
        public static final TagKey<Item> TRANSFORMABLE_ITEMS = createTag("transformable_items");

        // Creates a namespaced item tag key
        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, name));
        }
    }
}
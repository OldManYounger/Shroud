package net.oldmanyounger.shroud.trim;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.neoforged.neoforge.registries.DeferredItem;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.item.ModItems;

/**
 * Declares and bootstraps custom armor trim patterns for Shroud.
 *
 * <p>This class defines trim pattern keys and registers the corresponding trim
 * pattern data entries that reference smithing template items.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * smithing customization layer that integrates custom trim templates into
 * vanilla armor trim pattern systems.
 */
public class ModTrimPatterns {

    // Resource key for the Eventide trim pattern
    public static final ResourceKey<TrimPattern> EVENTIDE = ResourceKey.create(
            Registries.TRIM_PATTERN,
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide")
    );

    // Registers all Shroud trim patterns during bootstrap
    public static void bootstrap(BootstrapContext<TrimPattern> context) {
        register(context, ModItems.EVENTIDE_SMITHING_TEMPLATE, EVENTIDE);
    }

    // Builds and registers a single trim pattern entry
    private static void register(
            BootstrapContext<TrimPattern> context,
            DeferredItem<Item> item,
            ResourceKey<TrimPattern> key
    ) {
        TrimPattern trimPattern = new TrimPattern(
                key.location(),
                item.getDelegate(),
                Component.translatable(Util.makeDescriptionId("trim_pattern", key.location())),
                false
        );
        context.register(key, trimPattern);
    }
}
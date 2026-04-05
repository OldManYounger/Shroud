package net.oldmanyounger.shroud.trim;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.Map;

/**
 * Declares and bootstraps custom armor trim materials for Shroud.
 *
 * <p>This class defines trim material resource keys and registers corresponding
 * runtime trim material entries during data bootstrap.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * trim-content pipeline that integrates custom smithing aesthetics with the
 * vanilla armor trim system.
 */
public class ModTrimMaterials {

    // Resource key for the Eventide trim material
    public static final ResourceKey<TrimMaterial> EVENTIDE =
            ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "eventide"));

    // Registers all Shroud trim materials during bootstrap
    public static void bootstrap(BootstrapContext<TrimMaterial> context) {
        register(context, EVENTIDE, ModItems.EVENTIDE_INGOT.get(),
                Style.EMPTY.withColor(TextColor.parseColor("#031cfc").getOrThrow()),
                0.5F);
    }

    // Builds and registers a single trim material entry
    private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> trimKey, Item item,
                                 Style style, float itemModelIndex) {
        TrimMaterial trimmaterial = TrimMaterial.create(trimKey.location().getPath(), item, itemModelIndex,
                Component.translatable(Util.makeDescriptionId("trim_material", trimKey.location())).withStyle(style), Map.of());
        context.register(trimKey, trimmaterial);
    }
}
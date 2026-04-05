package net.oldmanyounger.shroud;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Defines mod configuration entries and validation rules for Shroud.
 *
 * <p>This class declares config values, builds the mod config spec, and provides
 * helper validation for resource-location-based item lists.
 *
 * <p>In the broader context of the project, this class is part of Shroud's runtime
 * configuration layer that enables tunable behavior without recompiling code.
 */
public class Config {

    // Config builder used to declare all entries
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Toggle for common-setup dirt block logging example
    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    // Example integer config value
    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    // Prefix text used with magic number logging output
    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // String list treated as item resource locations
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    // Built config specification used by NeoForge config system
    static final ModConfigSpec SPEC = BUILDER.build();

    // Validates that a config list entry resolves to a registered item id
    private static boolean validateItemName(final Object obj) {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }
}
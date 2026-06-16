package net.oldmanyounger.shroud;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Defines Shroud's configuration specification.
 *
 * <p>This class is intentionally minimal until Shroud has real configurable
 * gameplay, worldgen, rendering, or compatibility options.
 *
 * <p>In the broader context of the project, this class provides a stable place
 * for future config values while avoiding leftover template settings.
 */
public final class Config {

    // ==================================
    //  FIELDS
    // ==================================

    // Builder used to declare Shroud config entries before the spec is built
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Built config specification used when registering Shroud config with NeoForge
    public static final ModConfigSpec SPEC = BUILDER.build();

    // Utility class constructor
    private Config() {

    }
}

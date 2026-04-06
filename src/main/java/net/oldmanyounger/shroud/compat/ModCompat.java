package net.oldmanyounger.shroud.compat;

import net.neoforged.fml.ModList;
import net.oldmanyounger.shroud.Shroud;

/**
 * Compatibility feature flags and runtime detection helpers for external mods.
 *
 * <p>This class provides a single place to check optional integration availability
 * so common gameplay code can avoid direct hard dependencies on external APIs.
 *
 * <p>In the broader context of the project, this class is part of the compatibility
 * boundary that keeps Shroud stable when optional client mods are absent.
 */
public final class ModCompat {

    // ==================================
    //  FIELDS
    // ==================================

    // Mod id constant for Just Enough Items
    public static final String JEI_MOD_ID = "jei";

    // Cached optional integration state
    private static boolean jeiLoaded = false;

    // Utility class constructor
    private ModCompat() {

    }

    // ==================================
    //  INITIALIZATION
    // ==================================

    // Detects optional mods and logs integration state
    public static void initialize() {
        jeiLoaded = ModList.get().isLoaded(JEI_MOD_ID);

        if (jeiLoaded) {
            Shroud.LOGGER.info("Detected optional mod integration: JEI");
        } else {
            Shroud.LOGGER.info("Optional mod not present: JEI integration disabled");
        }
    }

    // ==================================
    //  ACCESSORS
    // ==================================

    // Returns true when JEI is present in the active runtime
    public static boolean isJeiLoaded() {
        return jeiLoaded;
    }
}
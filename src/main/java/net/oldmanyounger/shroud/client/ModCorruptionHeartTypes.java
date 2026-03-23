package net.oldmanyounger.shroud.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.oldmanyounger.shroud.Shroud;

/**
 * Declares Shroud's custom heart type used by the Corruption effect.
 *
 * <p>This class provides the enum-extension backing for a new
 * {@link Gui.HeartType} entry, wiring the custom sprite set into Minecraft's
 * vanilla heart rendering pipeline. By supplying full, half, blinking, and
 * hardcore sprite references, the game can render corrupted hearts using the
 * same mechanics as built-in heart types such as frozen or withered hearts.
 *
 * <p>In the broader context of the project, this class is the rendering bridge
 * that lets Corruption alter the health HUD in a native-feeling way while still
 * preserving vanilla layout and behavior.
 */
public final class ModCorruptionHeartTypes {

    // Enum extension proxy that defines the custom corrupted heart type and its sprite set
    public static final EnumProxy<Gui.HeartType> CORRUPTED = new EnumProxy<>(
            Gui.HeartType.class,
            sprite("hud/heart/corrupted_heart_full"),
            sprite("hud/heart/corrupted_heart_full_blinking"),
            sprite("hud/heart/corrupted_heart_half"),
            sprite("hud/heart/corrupted_heart_half_blinking"),
            sprite("hud/heart/corrupted_heart_full"),
            sprite("hud/heart/corrupted_heart_full_blinking"),
            sprite("hud/heart/corrupted_heart_half"),
            sprite("hud/heart/corrupted_heart_half_blinking")
    );

    // Prevent instantiation because this class only exposes static heart-type helpers
    private ModCorruptionHeartTypes() {
    }

    // Returns the resolved corruption heart type value from the enum proxy
    public static Gui.HeartType corrupted() {
        return CORRUPTED.getValue();
    }

    // Builds a namespaced sprite location under Shroud's GUI heart texture path
    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, path);
    }
}
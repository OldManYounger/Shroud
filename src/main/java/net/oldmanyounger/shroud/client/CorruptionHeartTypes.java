package net.oldmanyounger.shroud.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.oldmanyounger.shroud.Shroud;

public final class CorruptionHeartTypes {
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

    private CorruptionHeartTypes() {
    }

    public static Gui.HeartType corrupted() {
        return CORRUPTED.getValue();
    }

    private static ResourceLocation sprite(String path) {
        return ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, path);
    }
}
package net.oldmanyounger.shroud.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.client.CorruptionHeartRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.HeartType.class)
public abstract class GuiHeartTypeCorruptionMixin {
    @Unique
    private static final ResourceLocation SHROUD$CORRUPTION_CONTAINER =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "hud/heart/corruption_container");

    @Unique
    private static final ResourceLocation SHROUD$CORRUPTION_FULL =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "hud/heart/corruption_full");

    @Unique
    private static final ResourceLocation SHROUD$CORRUPTION_HALF =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "hud/heart/corruption_half");

    @Unique
    private static final ResourceLocation SHROUD$CORRUPTION_FULL_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "hud/heart/corruption_full_blinking");

    @Unique
    private static final ResourceLocation SHROUD$CORRUPTION_HALF_BLINKING =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "hud/heart/corruption_half_blinking");

    @Inject(method = "getSprite", at = @At("HEAD"), cancellable = true)
    private void shroud$swapHeartSprite(boolean hardcore, boolean half, boolean blinking, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!CorruptionHeartRenderState.isActive()) {
            return;
        }

        String heartTypeName = ((Enum<?>) (Object) this).name();

        if ("CONTAINER".equals(heartTypeName)) {
            cir.setReturnValue(SHROUD$CORRUPTION_CONTAINER);
            return;
        }

        if ("ABSORBING".equals(heartTypeName)) {
            return;
        }

        if (half) {
            cir.setReturnValue(blinking ? SHROUD$CORRUPTION_HALF_BLINKING : SHROUD$CORRUPTION_HALF);
        } else {
            cir.setReturnValue(blinking ? SHROUD$CORRUPTION_FULL_BLINKING : SHROUD$CORRUPTION_FULL);
        }
    }
}
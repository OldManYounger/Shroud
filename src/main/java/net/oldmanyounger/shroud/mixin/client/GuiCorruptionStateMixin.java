package net.oldmanyounger.shroud.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.oldmanyounger.shroud.client.CorruptionHeartRenderState;
import net.oldmanyounger.shroud.effect.ModMobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiCorruptionStateMixin {
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void shroud$beginCorruptionHeartRender(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        CorruptionHeartRenderState.setActive(player != null && player.hasEffect(ModMobEffects.CORRUPTION));
    }

    @Inject(method = "renderPlayerHealth", at = @At("RETURN"))
    private void shroud$endCorruptionHeartRender(GuiGraphics guiGraphics, CallbackInfo ci) {
        CorruptionHeartRenderState.clear();
    }
}
package net.oldmanyounger.shroud.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Shroud.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> CORRUPTION =
            MOB_EFFECTS.register("corruption",
                    () -> new CorruptionMobEffect(MobEffectCategory.HARMFUL, 0x5B2A78));

    private ModMobEffects() {
    }
}
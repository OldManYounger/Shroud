package net.oldmanyounger.shroud.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oldmanyounger.shroud.Shroud;

/**
 * Registers all custom Mob Effects used by Shroud.
 *
 * <p>This class owns the deferred effect registry and declares each effect entry
 * so NeoForge can initialize and bind them at the correct lifecycle stage.
 *
 * <p>In the broader context of the project, this class is part of the status-effect
 * registration pipeline that exposes Shroud-specific gameplay modifiers (such as
 * Corruption) to entities, items, and other gameplay systems.
 */
public final class ModMobEffects {

    // Deferred register for all Shroud Mob Effects.
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Shroud.MOD_ID);

    // Corruption effect entry used across gameplay systems.
    public static final DeferredHolder<MobEffect, MobEffect> CORRUPTION =
            MOB_EFFECTS.register("corruption",
                    () -> new ModCorruptionMobEffect(MobEffectCategory.HARMFUL, 0x5B2A78));

    // Prevents instantiation of this registry holder class.
    private ModMobEffects() {
    }
}
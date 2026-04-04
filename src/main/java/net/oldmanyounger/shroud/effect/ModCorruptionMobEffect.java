package net.oldmanyounger.shroud.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.oldmanyounger.shroud.Shroud;

/**
 * Defines the Corruption status effect behavior for living entities.
 *
 * <p>This effect continuously enforces a temporary maximum-health cap while active,
 * clamps current health down to the allowed amount, and provides helpers to
 * apply/remove the cap and inspect blocked health for UI or gameplay logic.
 *
 * <p>In the broader context of the project, this class is part of Shroud's
 * progression and survival-pressure systems, giving Corruption a persistent and
 * mechanically clear penalty that integrates with vanilla attribute handling.
 */
public class ModCorruptionMobEffect extends MobEffect {

    // Hard cap for effective health while Corruption is active (5 full hearts).
    public static final float SAFE_HEALTH = 10.0F;

    // Stable modifier ID used to apply and remove the temporary max-health reduction.
    public static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "corruption_max_health");

    // Creates the effect with the supplied category and display color.
    public ModCorruptionMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    // Applies the health cap every tick and clamps current health if above the cap.
    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        applyCorruptionHealthCap(livingEntity);

        float allowedHealth = getAllowedHealth(livingEntity);
        if (livingEntity.getHealth() > allowedHealth) {
            livingEntity.setHealth(allowedHealth);
        }

        return true;
    }

    // Runs the Corruption effect logic on every tick for active entities.
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    // Applies or refreshes the max-health cap modifier on the target entity.
    public static void applyCorruptionHealthCap(LivingEntity livingEntity) {
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);

        double uncorruptedMaxHealth = maxHealth.getValue();
        if (uncorruptedMaxHealth > SAFE_HEALTH) {
            maxHealth.addTransientModifier(new AttributeModifier(
                    MAX_HEALTH_MODIFIER_ID,
                    SAFE_HEALTH - uncorruptedMaxHealth,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }
    }

    // Removes the Corruption health-cap modifier if it is currently present.
    public static void removeCorruptionHealthCap(LivingEntity livingEntity) {
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID) != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
    }

    // Returns the effective health limit the entity is allowed to keep while corrupted.
    public static float getAllowedHealth(LivingEntity livingEntity) {
        return Math.min(SAFE_HEALTH, (float) getUncorruptedMaxHealth(livingEntity));
    }

    // Returns the entity max health value with the Corruption modifier logically removed.
    public static double getUncorruptedMaxHealth(LivingEntity livingEntity) {
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return livingEntity.getMaxHealth();
        }

        AttributeModifier modifier = maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID);
        double currentMaxHealth = maxHealth.getValue();

        return modifier == null ? currentMaxHealth : currentMaxHealth - modifier.amount();
    }

    // Returns whole-number health points currently blocked by Corruption.
    public static int getBlockedHealthPoints(LivingEntity livingEntity) {
        return Math.max(0, Mth.ceil((float) getUncorruptedMaxHealth(livingEntity) - getAllowedHealth(livingEntity)));
    }
}
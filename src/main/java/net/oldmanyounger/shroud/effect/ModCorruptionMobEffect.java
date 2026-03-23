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

public class ModCorruptionMobEffect extends MobEffect {
    public static final float SAFE_HEALTH = 10.0F; // 5 full hearts
    public static final ResourceLocation MAX_HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(Shroud.MOD_ID, "corruption_max_health");

    public ModCorruptionMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public boolean applyEffectTick(ServerLevel level, LivingEntity livingEntity, int amplifier) {
        applyCorruptionHealthCap(livingEntity);

        float allowedHealth = getAllowedHealth(livingEntity);
        if (livingEntity.getHealth() > allowedHealth) {
            livingEntity.setHealth(allowedHealth);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

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

    public static void removeCorruptionHealthCap(LivingEntity livingEntity) {
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID) != null) {
            maxHealth.removeModifier(MAX_HEALTH_MODIFIER_ID);
        }
    }

    public static float getAllowedHealth(LivingEntity livingEntity) {
        return Math.min(SAFE_HEALTH, (float) getUncorruptedMaxHealth(livingEntity));
    }

    public static double getUncorruptedMaxHealth(LivingEntity livingEntity) {
        AttributeInstance maxHealth = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return livingEntity.getMaxHealth();
        }

        AttributeModifier modifier = maxHealth.getModifier(MAX_HEALTH_MODIFIER_ID);
        double currentMaxHealth = maxHealth.getValue();

        return modifier == null ? currentMaxHealth : currentMaxHealth - modifier.amount();
    }

    public static int getBlockedHealthPoints(LivingEntity livingEntity) {
        return Math.max(0, Mth.ceil((float) getUncorruptedMaxHealth(livingEntity) - getAllowedHealth(livingEntity)));
    }
}
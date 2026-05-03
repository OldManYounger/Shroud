package net.oldmanyounger.shroud.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.warden.Warden;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.entity.ModEntities;
import net.oldmanyounger.shroud.entity.custom.GloamEyedAmalgamEntity;
import net.oldmanyounger.shroud.portal.ShroudDimensions;

/**
 * Replaces sculk-shrieker Warden summons with Gloam Eyed Amalgam summons in the Shroud dimension.
 *
 * <p>This handler marks triggered Warden spawns during finalization, then replaces only the
 * successfully joining marked Warden. This avoids creating one replacement per vanilla spawn attempt.
 *
 * <p>In the broader context of the project, this class adapts vanilla deep-dark summoning
 * behavior into Shroud-specific boss encounter behavior without replacing the vanilla block.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModShriekerSummonEvents {

    // Persistent marker used to identify Wardens created by sculk-shrieker triggered spawning
    private static final String REPLACE_TRIGGERED_WARDEN_KEY = "shroud_replace_triggered_warden";

    // Prevents instantiation of this static event handler class
    private ModShriekerSummonEvents() {
    }

    // Marks triggered Warden spawns in the Shroud dimension without cancelling vanilla spawn attempts
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ShroudDimensions.SHROUD_LEVEL)) return;
        if (event.getSpawnType() != MobSpawnType.TRIGGERED) return;
        if (!(event.getEntity() instanceof Warden)) return;
        if (event.getEntity().getType() != EntityType.WARDEN) return;

        event.getEntity().getPersistentData().putBoolean(REPLACE_TRIGGERED_WARDEN_KEY, true);
    }

    // Replaces the one Warden that actually joins the level with a single arising Amalgam
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.dimension().equals(ShroudDimensions.SHROUD_LEVEL)) return;
        if (!(event.getEntity() instanceof Warden warden)) return;
        if (warden.getType() != EntityType.WARDEN) return;
        if (!warden.getPersistentData().getBoolean(REPLACE_TRIGGERED_WARDEN_KEY)) return;

        GloamEyedAmalgamEntity amalgam = ModEntities.GLOAM_EYED_AMALGAM.get().create(serverLevel);
        if (amalgam == null) return;

        amalgam.moveTo(warden.getX(), warden.getY(), warden.getZ(), warden.getYRot(), warden.getXRot());
        amalgam.finalizeSpawn(
                serverLevel,
                serverLevel.getCurrentDifficultyAt(amalgam.blockPosition()),
                MobSpawnType.TRIGGERED,
                null
        );

        amalgam.beginAriseFromSummon();

        if (serverLevel.addFreshEntity(amalgam)) {
            event.setCanceled(true);
            amalgam.spawnInitialAriseParticles(serverLevel);
        }
    }
}

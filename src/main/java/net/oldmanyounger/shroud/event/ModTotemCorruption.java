package net.oldmanyounger.shroud.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages delayed corruption of offhand Totems of Undying into modded corrupted totems.
 *
 * <p>This class schedules short delayed conversions, emits corruption buildup particles,
 * swaps the offhand item when the delay expires, and cleans up pending state as needed.
 *
 * <p>In the broader context of the project, this class is part of Shroud's combat
 * consequence systems that transform defensive vanilla mechanics into thematic
 * corruption outcomes tied to hostile mob interactions.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class ModTotemCorruption {

    // ==================================
    //  FIELDS
    // ==================================

    // Minimum conversion delay in ticks
    private static final int MIN_DELAY_TICKS = 2;

    // Maximum conversion delay in ticks
    private static final int MAX_DELAY_TICKS = 2;

    // Pending corruption state keyed by player UUID
    private static final Map<UUID, PendingCorruption> PENDING = new HashMap<>();

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Prevents instantiation of this static event handler class
    private ModTotemCorruption() {
    }

    // ==================================
    //  SCHEDULING
    // ==================================

    // Schedules an offhand totem corruption if one is not already pending
    public static void schedule(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        PENDING.computeIfAbsent(player.getUUID(), ignored -> {
            int delay = MIN_DELAY_TICKS + serverLevel.random.nextInt(MAX_DELAY_TICKS - MIN_DELAY_TICKS + 1);
            long convertGameTime = serverLevel.getGameTime() + delay;

            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.SCULK_CLICKING,
                    SoundSource.HOSTILE,
                    0.7F,
                    1.15F
            );

            return new PendingCorruption(convertGameTime);
        });
    }

    // ==================================
    //  TICK HANDLER
    // ==================================

    // Advances pending corruption state and performs conversion when timer elapses
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        PendingCorruption pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return;
        }

        if (!player.isAlive() || player.isRemoved()) {
            PENDING.remove(player.getUUID());
            return;
        }

        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.is(Items.TOTEM_OF_UNDYING)) {
            PENDING.remove(player.getUUID());
            return;
        }

        spawnCorruptionBlob(serverLevel, player);

        if (serverLevel.getGameTime() < pending.convertGameTime()) {
            return;
        }

        int count = offhandStack.getCount();
        ItemStack corruptedTotem = new ItemStack(ModItems.TOTEM_OF_LAST_BREATH.get(), count);
        player.setItemInHand(InteractionHand.OFF_HAND, corruptedTotem);

        Vec3 offhandPos = getOffhandPosition(player);

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                offhandPos.x,
                offhandPos.y,
                offhandPos.z,
                18,
                0.15D,
                0.2D,
                0.15D,
                0.03D
        );

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                offhandPos.x,
                offhandPos.y,
                offhandPos.z,
                12,
                0.12D,
                0.12D,
                0.12D,
                0.02D
        );

        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SCULK_CATALYST_BLOOM,
                SoundSource.HOSTILE,
                0.85F,
                1.2F
        );

        PENDING.remove(player.getUUID());
    }

    // ==================================
    //  PARTICLES / POSITION HELPERS
    // ==================================

    // Spawns interim corruption particles near the player's offhand
    private static void spawnCorruptionBlob(ServerLevel serverLevel, Player player) {
        Vec3 offhandPos = getOffhandPosition(player);

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                offhandPos.x,
                offhandPos.y + 0.04D,
                offhandPos.z,
                5,
                0.09D,
                0.12D,
                0.09D,
                0.01D
        );

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                offhandPos.x,
                offhandPos.y,
                offhandPos.z,
                6,
                0.08D,
                0.10D,
                0.08D,
                0.01D
        );

        serverLevel.sendParticles(
                ParticleTypes.SCULK_SOUL,
                offhandPos.x,
                offhandPos.y - 0.03D,
                offhandPos.z,
                3,
                0.04D,
                0.06D,
                0.04D,
                0.0D
        );
    }

    // Computes an approximate world-space offhand position relative to facing direction
    private static Vec3 getOffhandPosition(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z).normalize();

        if (flatLook.lengthSqr() < 1.0E-6D) {
            flatLook = new Vec3(0.0D, 0.0D, 1.0D);
        }

        double offhandSide = player.getMainArm() == HumanoidArm.RIGHT ? -1.0D : 1.0D;

        Vec3 side = new Vec3(-flatLook.z, 0.0D, flatLook.x).scale(0.32D * offhandSide);
        Vec3 forward = flatLook.scale(0.18D);

        return player.position()
                .add(0.0D, player.getBbHeight() * 0.62D, 0.0D)
                .add(side)
                .add(forward);
    }

    // ==================================
    //  RECORDS
    // ==================================

    // Stores conversion deadline game time for a pending corruption
    private record PendingCorruption(long convertGameTime) {
    }
}
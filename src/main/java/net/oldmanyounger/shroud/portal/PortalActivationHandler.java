package net.oldmanyounger.shroud.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.sound.ModSounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles delayed Shroud portal activation from player frame interactions.
 *
 * <p>This class listens for valid right-click activation attempts, applies immediate
 * activation feedback, queues pending activations, and processes countdown completion
 * on server ticks to attempt final portal creation.
 *
 * <p>In the broader context of the project, this class is part of Shroud's world
 * transition systems that gate custom dimension travel behind themed interaction and
 * delayed ritual-style activation behavior.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class PortalActivationHandler {

    // ==================================
    //  FIELDS
    // ==================================

    // Ticks to wait before attempting portal creation
    private static final int PORTAL_ACTIVATION_DELAY_TICKS = 230;

    // Darkness duration applied to the activating player
    private static final int DARKNESS_DURATION_TICKS = 235;

    // Pending activation queue processed during server ticks
    private static final List<PendingActivation> PENDING_ACTIVATIONS = new ArrayList<>();

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Prevents instantiation of this static event handler class
    private PortalActivationHandler() {
    }

    // ==================================
    //  INTERACTION HANDLER
    // ==================================

    // Queues delayed activation when a valid frame block is clicked with an echo shard
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        // Runs activation logic only on the logical server
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();

        // Requires echo shard activation item
        if (!stack.is(Items.ECHO_SHARD)) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Requires approved frame materials
        if (!(clickedState.is(Blocks.DEEPSLATE_BRICKS) || clickedState.is(Blocks.REINFORCED_DEEPSLATE))) {
            return;
        }

        // Requires server level instance
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Skips duplicate pending activation entries for same level and frame position
        if (isAlreadyPending(serverLevel, clickedPos)) {
            return;
        }

        // Requires valid frame geometry before queueing activation
        if (!ShroudPortalHelper.canCreatePortal(serverLevel, clickedPos)) {
            return;
        }

        Player player = event.getEntity();

        // Applies Darkness feedback to activating player
        if (player != null) {
            player.addEffect(
                    new MobEffectInstance(
                            MobEffects.DARKNESS,
                            DARKNESS_DURATION_TICKS,
                            0,
                            false,
                            true
                    )
            );
        }

        // Plays activation-start sound at frame
        serverLevel.playSound(
                null,
                clickedPos,
                ModSounds.SCULK_PORTAL_ACTIVATE.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        // Adds delayed activation entry
        PENDING_ACTIVATIONS.add(new PendingActivation(serverLevel, clickedPos, PORTAL_ACTIVATION_DELAY_TICKS));

        // Marks interaction handled and cancels further processing
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ==================================
    //  SERVER TICK PROCESSING
    // ==================================

    // Processes pending activation countdowns and attempts portal creation when ready
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        Iterator<PendingActivation> iterator = PENDING_ACTIVATIONS.iterator();

        // Updates all queued entries each server tick
        while (iterator.hasNext()) {
            PendingActivation pending = iterator.next();

            // Processes only entries that belong to this server instance
            if (pending.level().getServer() != server) {
                continue;
            }

            int remaining = pending.ticksRemaining() - 1;

            // Continues countdown until activation time is reached
            if (remaining > 0) {
                pending.setTicksRemaining(remaining);
                continue;
            }

            // Attempts portal creation and plays completion roar at frame position
            ServerLevel level = pending.level();
            BlockPos framePos = pending.framePos();

            ShroudPortalHelper.tryCreatePortal(level, framePos);

            level.playSound(
                    null,
                    framePos,
                    SoundEvents.WARDEN_ROAR,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );

            // Removes processed entry regardless of success result
            iterator.remove();
        }
    }

    // ==================================
    //  HELPERS
    // ==================================

    // Returns true when a matching pending activation already exists
    private static boolean isAlreadyPending(ServerLevel level, BlockPos framePos) {
        for (PendingActivation pending : PENDING_ACTIVATIONS) {
            if (pending.level() == level && pending.framePos().equals(framePos)) {
                return true;
            }
        }

        return false;
    }

    // ==================================
    //  INNER TYPES
    // ==================================

    // Represents a queued portal activation with countdown state
    private static final class PendingActivation {

        // Target level for activation
        private final ServerLevel level;

        // Frame position used for portal creation attempt
        private final BlockPos framePos;

        // Remaining countdown ticks before processing
        private int ticksRemaining;

        // Creates a pending activation entry
        private PendingActivation(ServerLevel level, BlockPos framePos, int ticksRemaining) {
            this.level = level;
            this.framePos = framePos.immutable();
            this.ticksRemaining = ticksRemaining;
        }

        // Returns the target level
        public ServerLevel level() {
            return level;
        }

        // Returns the frame position
        public BlockPos framePos() {
            return framePos;
        }

        // Returns remaining countdown ticks
        public int ticksRemaining() {
            return ticksRemaining;
        }

        // Updates remaining countdown ticks
        public void setTicksRemaining(int ticksRemaining) {
            this.ticksRemaining = ticksRemaining;
        }
    }
}
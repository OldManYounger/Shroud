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
 * Handles activation of Shroud portals by tracking delayed activations and
 * wiring player interaction into the Shroud portal creation logic.
 * <p>
 * Player interaction flow:
 * <ul>
 *     <li>Player right-clicks a valid frame block with an echo shard.</li>
 *     <li>Immediately apply Darkness and play a custom activation sound.</li>
 *     <li>After a fixed delay, attempt to light the portal and play a Warden roar.</li>
 * </ul>
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class PortalActivationHandler {

    /** Number of ticks to wait after activation before attempting to light the portal */
    private static final int PORTAL_ACTIVATION_DELAY_TICKS = 230;

    /** Duration of the Darkness status effect applied to the activating player */
    private static final int DARKNESS_DURATION_TICKS = 235;

    /** Pending portal activations that are counted down and processed on server ticks */
    private static final List<PendingActivation> PENDING_ACTIVATIONS = new ArrayList<>();

    /** Private constructor to prevent instantiation of this static utility handler */
    private PortalActivationHandler() {
    }

    /** Handles right-click interactions on blocks and queues a delayed portal activation for valid frames */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        // Only perform activation logic on the logical server
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();

        // Require the activation item to be an echo shard
        if (!stack.is(Items.ECHO_SHARD)) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // Only allow activation on the configured frame materials
        if (!(clickedState.is(Blocks.DEEPSLATE_BRICKS) || clickedState.is(Blocks.REINFORCED_DEEPSLATE))) {
            return;
        }

        // Ensure we are working with a ServerLevel instance
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Avoid creating duplicate pending activations for the same frame position and level
        if (isAlreadyPending(serverLevel, clickedPos)) {
            return;
        }

        // Only proceed if a valid Shroud portal frame can actually be created here
        if (!ShroudPortalHelper.canCreatePortal(serverLevel, clickedPos)) {
            return;
        }

        Player player = event.getEntity();

        // Apply the Darkness effect to the activating player if present
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

        // Play a custom activation sound at the frame position to signal portal activation start
        serverLevel.playSound(
                null,
                clickedPos,
                ModSounds.SCULK_PORTAL_ACTIVATE.get(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );


        // Queue a delayed activation entry for this frame position
        PENDING_ACTIVATIONS.add(new PendingActivation(serverLevel, clickedPos, PORTAL_ACTIVATION_DELAY_TICKS));

        // Mark the interaction as handled and prevent further processing by other handlers or vanilla
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** Processes all pending portal activations once per server tick, counting down and lighting portals when ready */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();

        Iterator<PendingActivation> iterator = PENDING_ACTIVATIONS.iterator();

        // Walk through all queued activations and update their countdown state
        while (iterator.hasNext()) {
            PendingActivation pending = iterator.next();

            // Only process activations that belong to the current server instance
            if (pending.level().getServer() != server) {
                continue;
            }

            int remaining = pending.ticksRemaining() - 1;

            // If there is still time remaining, update the countdown and continue
            if (remaining > 0) {
                pending.setTicksRemaining(remaining);
                continue;
            }

            // When the countdown reaches zero, attempt to create the portal around this frame and play a Warden roar
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

            // Remove the activation entry regardless of whether portal creation succeeded
            iterator.remove();
        }
    }

    /** Returns true if there is already a pending activation for the given frame position in the given level */
    private static boolean isAlreadyPending(ServerLevel level, BlockPos framePos) {
        for (PendingActivation pending : PENDING_ACTIVATIONS) {
            if (pending.level() == level && pending.framePos().equals(framePos)) {
                return true;
            }
        }

        return false;
    }

    /** Represents a single pending portal activation with an associated countdown timer */
    private static final class PendingActivation {
        private final ServerLevel level;
        private final BlockPos framePos;
        private int ticksRemaining;

        /** Creates a new pending activation associated with a specific level, frame position, and delay */
        private PendingActivation(ServerLevel level, BlockPos framePos, int ticksRemaining) {
            this.level = level;
            this.framePos = framePos.immutable();
            this.ticksRemaining = ticksRemaining;
        }

        /** Returns the level in which this activation will attempt to light the portal */
        public ServerLevel level() {
            return level;
        }

        /** Returns the frame position that this activation will use when creating the portal */
        public BlockPos framePos() {
            return framePos;
        }

        /** Returns the remaining ticks before this activation will attempt to light the portal */
        public int ticksRemaining() {
            return ticksRemaining;
        }

        /** Updates the remaining ticks before this activation is processed */
        public void setTicksRemaining(int ticksRemaining) {
            this.ticksRemaining = ticksRemaining;
        }
    }
}

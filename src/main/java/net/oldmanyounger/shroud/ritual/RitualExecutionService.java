package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.entity.ModBindingPedestalBlockEntity;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Executes ritual transactions after a successful recipe match.
 *
 * <p>This service performs delayed ritual execution by locking participants, progressing
 * particle phases, applying staged mob damage, and committing output only after successful completion.
 *
 * <p>In the broader context of the project, this class is the transactional execution layer
 * for ritual crafting.
 */
@EventBusSubscriber(modid = Shroud.MOD_ID)
public final class RitualExecutionService {

    // ==================================
    //  FIELDS
    // ==================================

    // Ticks spent in coil-up phase
    private static final int PHASE_COIL_TICKS = 40;

    // Ticks spent in beam-to-altar phase
    private static final int PHASE_BEAM_TICKS = 50;

    // Ticks spent in altar-focus ball phase
    private static final int PHASE_BALL_TICKS = 30;

    // Height above reliquary where ritual energy converges
    private static final double FOCUS_HEIGHT_OFFSET = 1.55D;

    // Radius of swirl around each mob
    private static final double COIL_RADIUS = 0.42D;

    // Pending ritual queue processed during server ticks
    private static final List<PendingRitual> PENDING_RITUALS = new ArrayList<>();

    // Horizontal stale-lock pedestal scan radius from reliquary
    private static final int STALE_UNLOCK_RADIUS_XZ = 8;

    // Vertical stale-lock pedestal scan range above and below reliquary
    private static final int STALE_UNLOCK_RADIUS_Y = 1;

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Utility class constructor
    private RitualExecutionService() {

    }

    // ==================================
    //  ENTRYPOINT
    // ==================================

    // Starts a delayed ritual from a precomputed match context
    public static RitualExecutionResult execute(ServerLevel level,
                                                BlockPos reliquaryPos,
                                                Player player,
                                                ModCorruptedReliquaryBlockEntity reliquaryBe,
                                                RitualRecipeMatcher.RitualMatchContext matchContext) {
        RitualRecipe recipe = matchContext.recipe();

        // Revalidates before any lock or mutation
        RitualCommitValidator.ValidationResult preLockValidation =
                RitualCommitValidator.validateMatchStillHolds(level, reliquaryPos, reliquaryBe, recipe.id());

        if (!preLockValidation.isSuccess()) {
            return RitualExecutionResult.fail(preLockValidation.message());
        }

        List<ModBindingPedestalBlockEntity> participantPedestals = new ArrayList<>();
        for (RitualRecipeMatcher.PedestalSelection selection : matchContext.selectedPedestals()) {
            BlockEntity be = level.getBlockEntity(selection.pos());
            if (!(be instanceof ModBindingPedestalBlockEntity pedestalBe)) {
                return RitualExecutionResult.fail("Missing participating pedestal");
            }
            participantPedestals.add(pedestalBe);
        }

        reliquaryBe.setRitualLocked(true);
        for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
            pedestal.setRitualLocked(true);
        }

        // Revalidates again just before queueing
        RitualCommitValidator.ValidationResult preCommitValidation =
                RitualCommitValidator.validateMatchStillHolds(level, reliquaryPos, reliquaryBe, recipe.id());

        if (!preCommitValidation.isSuccess()) {
            unlockRitualLocks(reliquaryBe, participantPedestals);
            return RitualExecutionResult.fail(preCommitValidation.message());
        }

        int ritualDurationTicks = getRitualDurationTicks(recipe);
        reliquaryBe.startRitualVisual(level.getGameTime(), ritualDurationTicks);

        float[] damageAppliedByPedestal = new float[participantPedestals.size()];

        PENDING_RITUALS.add(new PendingRitual(
                level,
                reliquaryPos,
                recipe,
                reliquaryBe,
                participantPedestals,
                damageAppliedByPedestal,
                0
        ));

        return RitualExecutionResult.success("Ritual started");
    }

    // ==================================
    //  SERVER TICK PROCESSING
    // ==================================

    // Processes pending ritual progression on each server tick
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        Iterator<PendingRitual> iterator = PENDING_RITUALS.iterator();

        while (iterator.hasNext()) {
            PendingRitual pending = iterator.next();

            if (pending.level().getServer() != server) {
                continue;
            }

            boolean done = processPendingRitual(pending);
            if (done) {
                iterator.remove();
            }
        }
    }

    // Advances one ritual tick and returns true when ritual is finished or failed
    private static boolean processPendingRitual(PendingRitual pending) {
        ServerLevel level = pending.level();
        RitualRecipe recipe = pending.recipe();
        int ritualDurationTicks = getRitualDurationTicks(recipe);
        float totalDamagePerMob = Math.max(0.0F, recipe.mobDamagePerRequiredMob());

        List<ModBindingPedestalBlockEntity> participantPedestals = pending.participantPedestals();
        int pedestalCount = participantPedestals.size();

        List<LivingEntity> boundMobs = new ArrayList<>(pedestalCount);
        for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
            LivingEntity living = pedestal.getBoundLivingMob(level);
            if (living == null) {
                unlockRitualLocks(pending.reliquaryBe(), participantPedestals);
                return true;
            }
            boundMobs.add(living);
        }

        int nextTick = pending.ticksElapsed() + 1;
        emitProgressParticles(level, pending.reliquaryPos(), boundMobs, nextTick, ritualDurationTicks);

        if (totalDamagePerMob > 0.0F) {
            double damageProgress = Mth.clamp(nextTick / (double) PHASE_COIL_TICKS, 0.0D, 1.0D);
            float targetDamageByNow = (float) (totalDamagePerMob * damageProgress);

            for (int i = 0; i < pedestalCount; i++) {
                ModBindingPedestalBlockEntity pedestal = participantPedestals.get(i);
                float alreadyApplied = pending.damageAppliedByPedestal()[i];

                while ((alreadyApplied + 1.0F) <= targetDamageByNow) {
                    boolean damagedAndAlive = pedestal.damageBoundMob(1.0F);
                    if (!damagedAndAlive) {
                        unlockRitualLocks(pending.reliquaryBe(), participantPedestals);
                        return true;
                    }
                    alreadyApplied += 1.0F;
                }

                pending.damageAppliedByPedestal()[i] = alreadyApplied;
            }
        }

        if (nextTick < ritualDurationTicks) {
            pending.setTicksElapsed(nextTick);
            return false;
        }

        if (totalDamagePerMob > 0.0F) {
            for (int i = 0; i < pedestalCount; i++) {
                ModBindingPedestalBlockEntity pedestal = participantPedestals.get(i);
                float alreadyApplied = pending.damageAppliedByPedestal()[i];
                float remaining = totalDamagePerMob - alreadyApplied;

                if (remaining > 0.0F) {
                    boolean damagedAndAlive = pedestal.damageBoundMob(remaining);
                    if (!damagedAndAlive) {
                        unlockRitualLocks(pending.reliquaryBe(), participantPedestals);
                        return true;
                    }
                    pending.damageAppliedByPedestal()[i] = totalDamagePerMob;
                }
            }
        }

        boolean consumedAtCommit = pending.reliquaryBe().consumeRequirements(recipe.itemRequirements());
        if (!consumedAtCommit) {
            unlockRitualLocks(pending.reliquaryBe(), participantPedestals);
            return true;
        }

        ItemStack output = recipe.output().copy();
        routeOutput(level, pending.reliquaryPos(), output);
        emitCompletionParticles(level, pending.reliquaryPos());
        unlockRitualLocks(pending.reliquaryBe(), participantPedestals);

        return true;
    }

    // ==================================
    //  DURATION HELPERS
    // ==================================

    // Converts recipe duration seconds into clamped ritual ticks
    private static int getRitualDurationTicks(RitualRecipe recipe) {
        return Math.max(1, recipe.durationSeconds() * 20);
    }

    // ==================================
    //  PARTICLE VISUALS
    // ==================================

    // Emits ritual progression particles in strict phase order coil then beam then ball
    private static void emitProgressParticles(ServerLevel level,
                                              BlockPos reliquaryPos,
                                              List<LivingEntity> boundMobs,
                                              int ticksElapsed,
                                              int ritualDurationTicks) {
        double focusX = reliquaryPos.getX() + 0.5D;
        double focusY = reliquaryPos.getY() + FOCUS_HEIGHT_OFFSET;
        double focusZ = reliquaryPos.getZ() + 0.5D;

        int clampedTick = Mth.clamp(ticksElapsed, 1, ritualDurationTicks);

        int coilEnd = PHASE_COIL_TICKS;
        int beamEnd = PHASE_COIL_TICKS + PHASE_BEAM_TICKS;
        int ballEnd = PHASE_COIL_TICKS + PHASE_BEAM_TICKS + PHASE_BALL_TICKS;

        if (ballEnd > ritualDurationTicks) {
            ballEnd = ritualDurationTicks;
            beamEnd = Math.min(beamEnd, ballEnd);
            coilEnd = Math.min(coilEnd, beamEnd);
        }

        // Phase 1 only coil particles rising around each mob
        if (clampedTick <= coilEnd) {
            double coilProgress = clampedTick / (double) Math.max(1, coilEnd);

            for (int i = 0; i < boundMobs.size(); i++) {
                LivingEntity mob = boundMobs.get(i);

                double mobCenterX = mob.getX();
                double mobBaseY = mob.getY();
                double mobTopY = mob.getY() + 2.0D;
                double mobCenterZ = mob.getZ();

                double angle = (ticksElapsed * 0.40D) + (i * (Math.PI * 2.0D / Math.max(1, boundMobs.size())));
                double coilY = Mth.lerp(coilProgress, mobBaseY + 0.05D, mobTopY);

                double coilX = mobCenterX + Math.cos(angle) * COIL_RADIUS;
                double coilZ = mobCenterZ + Math.sin(angle) * COIL_RADIUS;

                level.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        coilX,
                        coilY,
                        coilZ,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

            return;
        }

        // Phase 2 only beam particles from mob head to altar focus
        if (clampedTick <= beamEnd) {
            double beamProgress = (clampedTick - coilEnd) / (double) Math.max(1, (beamEnd - coilEnd));

            for (LivingEntity mob : boundMobs) {
                double headX = mob.getX();
                double headY = mob.getY() + 2.0D;
                double headZ = mob.getZ();

                double beamX = Mth.lerp(beamProgress, headX, focusX);
                double beamY = Mth.lerp(beamProgress, headY, focusY);
                double beamZ = Mth.lerp(beamProgress, headZ, focusZ);

                level.sendParticles(
                        ParticleTypes.SCULK_SOUL,
                        beamX,
                        beamY,
                        beamZ,
                        1,
                        0.0D,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }

            return;
        }

        // Phase 3 only focus-ball particles boiling above the reliquary
        double ballProgress = (clampedTick - beamEnd) / (double) Math.max(1, (ballEnd - beamEnd));
        ballProgress = Mth.clamp(ballProgress, 0.0D, 1.0D);

        double ballRadius = 0.06D + (0.20D * ballProgress);
        int orbitCount = 4 + (int) Math.floor(ballProgress * 4.0D);

        for (int j = 0; j < orbitCount; j++) {
            double orbitAngle = (ticksElapsed * 0.25D) + (j * ((Math.PI * 2.0D) / Math.max(1, orbitCount)));
            double px = focusX + Math.cos(orbitAngle) * ballRadius;
            double py = focusY + Math.sin((ticksElapsed * 0.12D) + j) * (0.04D + (ballProgress * 0.03D));
            double pz = focusZ + Math.sin(orbitAngle) * ballRadius;

            level.sendParticles(
                    ParticleTypes.SCULK_SOUL,
                    px,
                    py,
                    pz,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    // Emits completion particles at the reliquary after a successful ritual
    private static void emitCompletionParticles(ServerLevel level, BlockPos reliquaryPos) {
        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                reliquaryPos.getX() + 0.5D,
                reliquaryPos.getY() + 1.20D,
                reliquaryPos.getZ() + 0.5D,
                24,
                0.35D,
                0.25D,
                0.35D,
                0.01D
        );
    }

    // ==================================
    //  OUTPUT ROUTING
    // ==================================

    // Routes ritual output to adjacent containers including below or north-side drop fallback
    private static void routeOutput(ServerLevel level, BlockPos reliquaryPos, ItemStack output) {
        if (output.isEmpty()) return;

        ItemStack remainder = output.copy();

        boolean insertedIntoContainer = tryInsertIntoAdjacentContainer(level, reliquaryPos, remainder);
        if (insertedIntoContainer) {
            return;
        }

        dropNorthOfReliquary(level, reliquaryPos, remainder);
    }

    // Tries to insert output stack into any container on four cardinal sides then below
    private static boolean tryInsertIntoAdjacentContainer(ServerLevel level, BlockPos reliquaryPos, ItemStack stack) {
        Direction[] directions = new Direction[]{
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST,
                Direction.DOWN
        };

        for (Direction direction : directions) {
            BlockPos containerPos = reliquaryPos.relative(direction);
            Container container = getContainerAt(level, containerPos);
            if (container == null) continue;

            insertIntoContainer(container, stack);
            container.setChanged();

            if (stack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    // Returns a container at the given position when present
    private static Container getContainerAt(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container container) {
            return container;
        }

        return null;
    }

    // Inserts as much as possible from stack into a container
    private static void insertIntoContainer(Container container, ItemStack stack) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (stack.isEmpty()) return;

            ItemStack existing = container.getItem(slot);

            if (existing.isEmpty()) {
                if (!container.canPlaceItem(slot, stack)) continue;

                int move = Math.min(stack.getCount(), Math.min(container.getMaxStackSize(), stack.getMaxStackSize()));
                ItemStack moved = stack.copyWithCount(move);
                container.setItem(slot, moved);
                stack.shrink(move);
                continue;
            }

            if (!ItemStack.isSameItemSameComponents(existing, stack)) continue;
            if (!container.canPlaceItem(slot, stack)) continue;

            int slotLimit = Math.min(container.getMaxStackSize(), existing.getMaxStackSize());
            int room = slotLimit - existing.getCount();
            if (room <= 0) continue;

            int move = Math.min(room, stack.getCount());
            existing.grow(move);
            stack.shrink(move);
            container.setItem(slot, existing);
        }
    }

    // Drops output one block north of reliquary and one block up
    private static void dropNorthOfReliquary(ServerLevel level, BlockPos reliquaryPos, ItemStack output) {
        if (output.isEmpty()) return;

        BlockPos dropPos = reliquaryPos.relative(Direction.NORTH).above();

        double x = dropPos.getX() + 0.5D;
        double y = dropPos.getY() + 0.05D;
        double z = dropPos.getZ() + 0.5D;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, output);
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    // ==================================
    // PENDING RITUAL LOGIC
    // ==================================

    // Clears stale reliquary and pedestal ritual locks when no pending ritual exists
    public static void clearStaleLockIfNotPending(ServerLevel level,
                                                  BlockPos reliquaryPos,
                                                  ModCorruptedReliquaryBlockEntity reliquaryBe) {
        if (!reliquaryBe.isRitualLocked()) return;

        boolean hasPending = hasPendingRitualAt(level, reliquaryPos);
        if (hasPending) return;

        reliquaryBe.setRitualLocked(false);
        reliquaryBe.clearRitualVisual();
        unlockNearbyPedestals(level, reliquaryPos);
    }

    // Returns true when a ritual queue entry exists for this reliquary position
    private static boolean hasPendingRitualAt(ServerLevel level, BlockPos reliquaryPos) {
        for (PendingRitual pending : PENDING_RITUALS) {
            if (pending.level() != level) continue;
            if (!pending.reliquaryPos().equals(reliquaryPos)) continue;
            return true;
        }
        return false;
    }

    // Unlocks nearby binding pedestals that may have stale ritual lock state
    private static void unlockNearbyPedestals(ServerLevel level, BlockPos reliquaryPos) {
        BlockPos min = reliquaryPos.offset(-STALE_UNLOCK_RADIUS_XZ, -STALE_UNLOCK_RADIUS_Y, -STALE_UNLOCK_RADIUS_XZ);
        BlockPos max = reliquaryPos.offset(STALE_UNLOCK_RADIUS_XZ, STALE_UNLOCK_RADIUS_Y, STALE_UNLOCK_RADIUS_XZ);

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof ModBindingPedestalBlockEntity pedestalBe)) continue;
            if (!pedestalBe.isRitualLocked()) continue;

            pedestalBe.setRitualLocked(false);
        }
    }

    // ==================================
    //  LOCK HELPERS
    // ==================================

    // Unlocks reliquary and all participating pedestals
    private static void unlockRitualLocks(ModCorruptedReliquaryBlockEntity reliquaryBe,
                                          List<ModBindingPedestalBlockEntity> participantPedestals) {
        reliquaryBe.setRitualLocked(false);
        reliquaryBe.clearRitualVisual();
        for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
            pedestal.setRitualLocked(false);
        }
    }

    // ==================================
    //  RESULT MODELS
    // ==================================

    /**
     * Result payload for ritual execution.
     *
     * <p>In the broader context of the project, this keeps activation UX and telemetry
     * decoupled from low-level execution details.
     */
    public static final class RitualExecutionResult {
        private final boolean success;
        private final String message;

        // Creates an execution result
        private RitualExecutionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Creates a success result
        public static RitualExecutionResult success(String message) {
            return new RitualExecutionResult(true, message);
        }

        // Creates a failure result
        public static RitualExecutionResult fail(String message) {
            return new RitualExecutionResult(false, message);
        }

        // Returns true when execution succeeded
        public boolean isSuccess() {
            return success;
        }

        // Returns execution message
        public String message() {
            return message;
        }
    }

    // ==================================
    //  INNER TYPES
    // ==================================

    // Represents one queued ritual process instance
    private static final class PendingRitual {
        private final ServerLevel level;
        private final BlockPos reliquaryPos;
        private final RitualRecipe recipe;
        private final ModCorruptedReliquaryBlockEntity reliquaryBe;
        private final List<ModBindingPedestalBlockEntity> participantPedestals;
        private final float[] damageAppliedByPedestal;
        private int ticksElapsed;

        // Creates a pending ritual
        private PendingRitual(ServerLevel level,
                              BlockPos reliquaryPos,
                              RitualRecipe recipe,
                              ModCorruptedReliquaryBlockEntity reliquaryBe,
                              List<ModBindingPedestalBlockEntity> participantPedestals,
                              float[] damageAppliedByPedestal,
                              int ticksElapsed) {
            this.level = level;
            this.reliquaryPos = reliquaryPos.immutable();
            this.recipe = recipe;
            this.reliquaryBe = reliquaryBe;
            this.participantPedestals = List.copyOf(participantPedestals);
            this.damageAppliedByPedestal = damageAppliedByPedestal;
            this.ticksElapsed = ticksElapsed;
        }

        // Returns ritual level
        public ServerLevel level() {
            return level;
        }

        // Returns reliquary block position
        public BlockPos reliquaryPos() {
            return reliquaryPos;
        }

        // Returns ritual recipe
        public RitualRecipe recipe() {
            return recipe;
        }

        // Returns reliquary block entity
        public ModCorruptedReliquaryBlockEntity reliquaryBe() {
            return reliquaryBe;
        }

        // Returns participating pedestals
        public List<ModBindingPedestalBlockEntity> participantPedestals() {
            return participantPedestals;
        }

        // Returns per-pedestal applied damage accumulator
        public float[] damageAppliedByPedestal() {
            return damageAppliedByPedestal;
        }

        // Returns elapsed ritual ticks
        public int ticksElapsed() {
            return ticksElapsed;
        }

        // Updates elapsed ritual ticks
        public void setTicksElapsed(int ticksElapsed) {
            this.ticksElapsed = ticksElapsed;
        }
    }
}
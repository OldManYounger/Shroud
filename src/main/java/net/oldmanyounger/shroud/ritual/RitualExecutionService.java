package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.oldmanyounger.shroud.block.entity.ModBindingPedestalBlockEntity;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;
import net.oldmanyounger.shroud.ritual.recipe.RitualRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes ritual transactions after a successful recipe match.
 *
 * <p>This service applies lock state, performs commit-time revalidation, consumes reliquary inputs,
 * damages selected bound mobs, emits output, and guarantees unlock cleanup.
 *
 * <p>In the broader context of the project, this class is the transactional execution layer
 * for ritual crafting.
 */
public final class RitualExecutionService {

    // Utility class constructor
    private RitualExecutionService() {

    }

    // Executes ritual from a precomputed match context
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
            var be = level.getBlockEntity(selection.pos());
            if (!(be instanceof ModBindingPedestalBlockEntity pedestalBe)) {
                return RitualExecutionResult.fail("Missing participating pedestal");
            }
            participantPedestals.add(pedestalBe);
        }

        reliquaryBe.setRitualLocked(true);
        for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
            pedestal.setRitualLocked(true);
        }

        try {
            // Revalidates again after lock acquisition just before mutation
            RitualCommitValidator.ValidationResult preCommitValidation =
                    RitualCommitValidator.validateMatchStillHolds(level, reliquaryPos, reliquaryBe, recipe.id());

            if (!preCommitValidation.isSuccess()) {
                return RitualExecutionResult.fail(preCommitValidation.message());
            }

            boolean consumed = reliquaryBe.consumeRequirements(recipe.itemRequirements());
            if (!consumed) {
                return RitualExecutionResult.fail("Failed to consume reliquary items");
            }

            float mobDamage = recipe.mobDamagePerRequiredMob();
            if (mobDamage > 0.0F) {
                for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
                    boolean damagedAndAlive = pedestal.damageBoundMob(mobDamage);
                    if (!damagedAndAlive) {
                        return RitualExecutionResult.fail("A required mob died or could not be damaged");
                    }
                }
            }

            ItemStack output = recipe.output().copy();
            routeOutput(level, reliquaryPos, output);

            emitCompletionParticles(level, reliquaryPos);
            return RitualExecutionResult.success("Ritual completed");
        } finally {
            reliquaryBe.setRitualLocked(false);
            for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
                pedestal.setRitualLocked(false);
            }
        }
    }

    // Routes ritual output to adjacent chest or north-side drop fallback
    private static void routeOutput(ServerLevel level, BlockPos reliquaryPos, ItemStack output) {
        if (output.isEmpty()) return;

        ItemStack remainder = output.copy();

        boolean insertedIntoChest = tryInsertIntoAdjacentChest(level, reliquaryPos, remainder);
        if (insertedIntoChest) {
            return;
        }

        dropNorthOfReliquary(level, reliquaryPos, remainder);
    }

    // Tries to insert output stack into any chest on the four cardinal sides
    private static boolean tryInsertIntoAdjacentChest(ServerLevel level, BlockPos reliquaryPos, ItemStack stack) {
        Direction[] directions = new Direction[]{
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        };

        for (Direction direction : directions) {
            BlockPos chestPos = reliquaryPos.relative(direction);
            BlockEntity be = level.getBlockEntity(chestPos);
            if (!(be instanceof ChestBlockEntity chest)) continue;

            insertIntoContainer(chest, stack);
            chest.setChanged();

            if (stack.isEmpty()) {
                return true;
            }
        }

        return false;
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

    // Emits completion particles at the reliquary after a successful ritual
    private static void emitCompletionParticles(ServerLevel level, BlockPos reliquaryPos) {
        level.sendParticles(
                ParticleTypes.SCULK_SOUL,
                reliquaryPos.getX() + 0.5D,
                reliquaryPos.getY() + 1.20D,
                reliquaryPos.getZ() + 0.5D,
                16,
                0.30D,
                0.20D,
                0.30D,
                0.01D
        );
    }

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
}
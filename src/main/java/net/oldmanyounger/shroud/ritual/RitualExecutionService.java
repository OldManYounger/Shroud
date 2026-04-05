package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
                    boolean damaged = pedestal.damageBoundMob(mobDamage);
                    if (!damaged) {
                        return RitualExecutionResult.fail("Failed to damage one or more required mobs");
                    }
                }
            }

            ItemStack output = recipe.output().copy();
            boolean added = player.addItem(output);
            if (!added) {
                net.minecraft.world.level.block.Block.popResource(level, reliquaryPos.above(), output);
            }

            return RitualExecutionResult.success("Ritual completed");
        } finally {
            reliquaryBe.setRitualLocked(false);
            for (ModBindingPedestalBlockEntity pedestal : participantPedestals) {
                pedestal.setRitualLocked(false);
            }
        }
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
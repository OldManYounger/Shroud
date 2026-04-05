package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;

import java.util.Optional;

/**
 * Performs commit-time ritual revalidation checks before state mutation.
 *
 * <p>This validator reruns matching against current world state to prevent stale-match execution
 * when blocks, entities, or inputs changed between activation and commit.
 *
 * <p>In the broader context of the project, this class protects ritual transaction integrity
 * across race conditions and world mutation timing.
 */
public final class RitualCommitValidator {

    // Utility class constructor
    private RitualCommitValidator() {

    }

    // Revalidates that a matching recipe still exists for current world state
    public static ValidationResult validateMatchStillHolds(ServerLevel level,
                                                           BlockPos reliquaryPos,
                                                           ModCorruptedReliquaryBlockEntity reliquaryBe,
                                                           ResourceLocation expectedRecipeId) {
        Optional<RitualRecipeMatcher.RitualMatchContext> current =
                RitualRecipeMatcher.findFirstMatch(level, reliquaryPos, reliquaryBe.copyItems());

        if (current.isEmpty()) {
            return ValidationResult.fail("No valid ritual match at commit time");
        }

        ResourceLocation foundId = current.get().recipe().id();
        if (!foundId.equals(expectedRecipeId)) {
            return ValidationResult.fail("Ritual match changed before commit");
        }

        return ValidationResult.success();
    }

    /**
     * Commit validation result payload.
     *
     * <p>In the broader context of the project, this object cleanly separates revalidation
     * concerns from execution concerns.
     */
    public static final class ValidationResult {
        private final boolean success;
        private final String message;

        // Creates a validation result
        private ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Creates a successful validation result
        public static ValidationResult success() {
            return new ValidationResult(true, "Validation passed");
        }

        // Creates a failed validation result
        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        // Returns true when validation passed
        public boolean isSuccess() {
            return success;
        }

        // Returns validation message
        public String message() {
            return message;
        }
    }
}
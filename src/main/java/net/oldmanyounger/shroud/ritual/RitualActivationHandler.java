package net.oldmanyounger.shroud.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.oldmanyounger.shroud.Shroud;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;

import java.util.Optional;

/**
 * Handles ritual activation attempts initiated from the Corrupted Reliquary.
 *
 * <p>This class validates activation preconditions, performs recipe matching, and executes ritual
 * transactions for successful matches.
 *
 * <p>In the broader context of the project, this class is the activation entrypoint that bridges
 * player interaction into ritual matching and execution.
 */
public final class RitualActivationHandler {

    // Utility class constructor
    private RitualActivationHandler() {

    }

    // Attempts ritual activation from a reliquary interaction context
    public static RitualActivationResult tryActivate(Level level, BlockPos reliquaryPos, Player player, ModCorruptedReliquaryBlockEntity reliquaryBe) {
        if (level.isClientSide) {
            return new RitualActivationResult(RitualActivationStatus.CLIENT_SIDE, Optional.empty(), Optional.empty(), Optional.empty());
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return new RitualActivationResult(RitualActivationStatus.NO_MATCH, Optional.empty(), Optional.empty(), Optional.of("Server level unavailable"));
        }

        if (reliquaryBe.isRitualLocked()) {
            return new RitualActivationResult(RitualActivationStatus.RELIQUARY_LOCKED, Optional.empty(), Optional.empty(), Optional.empty());
        }

        Optional<RitualRecipeMatcher.RitualMatchContext> match =
                RitualRecipeMatcher.findFirstMatch(level, reliquaryPos, reliquaryBe.copyItems());

        if (match.isEmpty()) {
            String debugReason = RitualRecipeMatcher.debugNoMatch(level, reliquaryPos, reliquaryBe.copyItems());
            Shroud.LOGGER.info("Ritual no-match at {} by {} -> {}", reliquaryPos, player.getName().getString(), debugReason);
            return new RitualActivationResult(RitualActivationStatus.NO_MATCH, Optional.empty(), Optional.empty(), Optional.of(debugReason));
        }

        RitualExecutionService.RitualExecutionResult execution =
                RitualExecutionService.execute(serverLevel, reliquaryPos, player, reliquaryBe, match.get());

        if (execution.isSuccess() == false) {
            return new RitualActivationResult(RitualActivationStatus.EXECUTION_FAILED, match, Optional.of(execution), Optional.empty());
        }

        return new RitualActivationResult(RitualActivationStatus.EXECUTED, match, Optional.of(execution), Optional.empty());
    }

    /**
     * Ritual activation result payload.
     *
     * <p>In the broader context of the project, this object separates interaction feedback from
     * execution mechanics so activation can be extended safely in phases.
     */
    public record RitualActivationResult(
            RitualActivationStatus status,
            Optional<RitualRecipeMatcher.RitualMatchContext> match,
            Optional<RitualExecutionService.RitualExecutionResult> execution,
            Optional<String> debugMessage
    ) {

    }

    /**
     * Ritual activation status values.
     *
     * <p>In the broader context of the project, these statuses provide stable branching points
     * for UX feedback and future instrumentation.
     */
    public enum RitualActivationStatus {
        CLIENT_SIDE,
        RELIQUARY_LOCKED,
        NO_MATCH,
        EXECUTION_FAILED,
        EXECUTED
    }
}
package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import net.oldmanyounger.shroud.block.entity.ModProjectingLightBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a block that owns a block entity responsible for creating and managing
 * projected invisible light sources in the world.
 *
 * <p>In the broader context of the project, this block is part of the mod's custom
 * lighting infrastructure. Rather than simply emitting light from its own block
 * state alone, it delegates to a {@link ModProjectingLightBlockEntity} that can
 * rebuild, maintain, and clear projected light placements around it. This makes
 * it suitable for more advanced environmental lighting setups such as fixtures,
 * ambient effects, or directional light projection that cannot be represented by
 * a normal static light-emitting block.
 *
 * <p>The block's responsibilities are intentionally narrow: it creates the block
 * entity, informs it when the block is placed or removed, and requests rebuilds
 * when neighboring changes might affect the projection pattern. Server-side
 * ticking is then used to let the block entity perform its managed lighting work.
 */
public class ModProjectingLightBlock extends Block implements EntityBlock {

    // Creates a new projecting light block with the supplied block properties
    public ModProjectingLightBlock(Properties properties) {
        super(properties);
    }

    // Notifies the block entity to rebuild its projected lights when the block is first placed
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        // Only run rebuild logic on the server when this is a true block replacement
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);

            // Ask the light block entity to refresh its projected light layout
            if (be instanceof ModProjectingLightBlockEntity lightBe) {
                lightBe.markForRebuild();
            }
        }
    }

    // Clears any projected light blocks when this block is removed or replaced
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // Only clear managed lights on the server when the block is actually being replaced
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);

            // Remove any invisible light placements owned by this block entity
            if (be instanceof ModProjectingLightBlockEntity lightBe) {
                lightBe.clearProjectedLights();
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // Requests a debounced rebuild whenever neighboring blocks change
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        // Neighbor-based rebuild checks are only relevant on the server
        if (level.isClientSide) return;

        BlockEntity be = level.getBlockEntity(pos);

        // Queue a rebuild instead of doing it immediately so repeated updates can be coalesced
        if (be instanceof ModProjectingLightBlockEntity lightBe) {
            lightBe.requestRebuildDebounced();
        }
    }

    // Creates the block entity instance used to manage projected light behavior
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModProjectingLightBlockEntity(pos, state);
    }

    // Supplies the server-side ticker that lets the block entity process rebuild and maintenance work
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // This block entity only needs ticking logic on the server
        if (level.isClientSide) return null;

        // Return the ticker only for the matching registered block entity type
        return type == ModBlockEntities.LIMBO_FLUORESCENT_LIGHT.get()
                ? (lvl, p, st, be) -> ((ModProjectingLightBlockEntity) be).serverTick()
                : null;
    }
}
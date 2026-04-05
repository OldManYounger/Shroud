package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the Corrupted Reliquary block shell that hosts the ritual block entity.
 *
 * <p>This block is intentionally thin and delegates most behavior to
 * {@link ModCorruptedReliquaryBlockEntity}, including corruption storage, pulse timing,
 * and ritual progression logic. The block itself is responsible for creating the
 * block entity, exposing a server ticker, and handling lightweight player interaction.
 *
 * <p>In the broader context of the project, this class acts as the world-facing
 * anchor for ritual crafting by connecting placed block state to persistent
 * server-side reliquary logic.
 */
public class ModCorruptedReliquaryBlock extends Block implements EntityBlock {

    // Creates the block with supplied properties
    public ModCorruptedReliquaryBlock(Properties properties) {
        super(properties);
    }

    // Creates the reliquary block entity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModCorruptedReliquaryBlockEntity(pos, state);
    }

    // Supplies server-side ticking for the reliquary block entity
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }

        return type == ModBlockEntities.CORRUPTED_RELIQUARY.get()
                ? (lvl, pos, blockState, be) -> ModCorruptedReliquaryBlockEntity.serverTick(
                lvl,
                pos,
                blockState,
                (ModCorruptedReliquaryBlockEntity) be
        )
                : null;
    }

    // Shows a quick empty-hand status readout from the reliquary
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ModCorruptedReliquaryBlockEntity reliquary) {
            player.displayClientMessage(
                    Component.literal("Corruption: " + reliquary.getStoredCorruption() + " / " + reliquary.getMaxCorruption()),
                    true
            );
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
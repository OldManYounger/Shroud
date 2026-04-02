package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import net.oldmanyounger.shroud.block.entity.ModSculkEmitterBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a directional block that owns a block entity responsible for driving
 * upward-oriented particle or emitter behavior.
 *
 * <p>The block stores a facing direction, supports rotation and mirroring, and
 * delegates its active behavior to {@link ModSculkEmitterBlockEntity}. This keeps
 * the block itself focused on placement and state management while the block
 * entity handles ongoing runtime logic.
 *
 * <p>In the broader context of the project, this class acts as infrastructure
 * for atmospheric or environmental emitter blocks that need both orientation and
 * server-side ticking, such as particle pillars, vents, or themed sculk-based
 * world props.
 */
public class ModParticlePillarBlock extends DirectionalBlock implements EntityBlock {

    // Codec used by Minecraft to serialize and recreate this custom directional block
    public static final MapCodec<ModParticlePillarBlock> CODEC =
            simpleCodec(ModParticlePillarBlock::new);

    // Creates the block and initializes its default facing to upward
    public ModParticlePillarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    // Returns the codec associated with this custom directional block type
    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    // Registers the facing property used by this block
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Determines the block's facing direction when first placed
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    // Rotates the stored facing direction when the block is rotated
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // Mirrors the stored facing direction when the block is mirrored
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Creates the block entity that handles this block's runtime emitter behavior
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModSculkEmitterBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ModSculkEmitterBlockEntity emitter) {
            emitter.updateWoolMuteState();
        }
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        BlockPos behindEmitter = pos.relative(facing.getOpposite());

        // Only recompute when the block behind (opposite emission direction) changes
        if (neighborPos.equals(behindEmitter) && level.getBlockEntity(pos) instanceof ModSculkEmitterBlockEntity emitter) {
            emitter.updateWoolMuteState();
        }
    }

    // Supplies the server-side ticker for the matching block entity type
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // The block entity only ticks on the server
        if (level.isClientSide) return null;

        // Return the ticker only for the registered sculk emitter block entity type
        return type == ModBlockEntities.SCULK_EMITTER.get()
                ? (lvl, p, st, be) -> ((ModSculkEmitterBlockEntity) be).serverTick()
                : null;
    }
}
package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.oldmanyounger.shroud.block.entity.ModBindingPedestalBlockEntity;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

/**
 * World block for automatically capturing and holding one mob on a Binding Pedestal.
 *
 * <p>This block binds the first eligible mob that enters pedestal space and delegates all
 * persistent mob-holding behavior to its block entity ticker. Binding is intentionally broken
 * only when the pedestal is removed.
 *
 * <p>In the broader context of the project, this class establishes the pedestal-side gameplay
 * surface for mob inputs that later ritual systems can query.
 */
public class ModBindingPedestalBlock extends BaseEntityBlock {

    // ==================================
    //  FIELDS
    // ==================================

    // Codec used by Minecraft to serialize and recreate this block type
    public static final MapCodec<ModBindingPedestalBlock> CODEC = simpleCodec(ModBindingPedestalBlock::new);

    // Full-height pedestal shape with side indent style
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D),
            Block.box(3.0D, 4.0D, 3.0D, 13.0D, 12.0D, 13.0D),
            Block.box(1.5D, 12.0D, 1.5D, 14.5D, 16.0D, 14.5D)
    );

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a binding pedestal block with the supplied block properties
    public ModBindingPedestalBlock(Properties properties) {
        super(properties);
    }

    // ==================================
    //  BLOCK BASICS
    // ==================================

    // Returns the codec for this block type
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Uses the normal baked block model renderer for in-world block rendering
    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Creates the binding pedestal block entity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModBindingPedestalBlockEntity(pos, state);
    }

    // ==================================
    //  SHAPE
    // ==================================

    // Returns the custom full-height outline shape
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Returns collision shape matching the visible pedestal footprint
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ==================================
    //  WORLD INTERACTION
    // ==================================

    // Captures an eligible mob when it enters pedestal space
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (level.isClientSide) return;

        ModBindingPedestalBlockEntity pedestal = getPedestalEntity(level, pos);
        if (pedestal != null) {
            pedestal.tryBindEntity(entity);
        }
    }

    // Releases bound state when the block is removed
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            ModBindingPedestalBlockEntity pedestal = getPedestalEntity(level, pos);
            if (pedestal != null) {
                pedestal.releaseBoundMob();
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ==================================
    //  TICKER
    // ==================================

    // Supplies the server-side ticker for bound mob hold behavior
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return type == ModBlockEntities.BINDING_PEDESTAL.get()
                ? (lvl, p, st, be) -> ((ModBindingPedestalBlockEntity) be).serverTick()
                : null;
    }

    // ==================================
    //  INTERNAL HELPERS
    // ==================================

    // Resolves this position's binding pedestal block entity when present
    @Nullable
    private ModBindingPedestalBlockEntity getPedestalEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ModBindingPedestalBlockEntity pedestal) {
            return pedestal;
        }
        return null;
    }
}
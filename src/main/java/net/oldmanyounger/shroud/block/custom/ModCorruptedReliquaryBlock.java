package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Block implementation for the Corrupted Reliquary item-input structure.
 *
 * <p>This block routes world interactions into reliquary storage behavior, including
 * single-item insertion on right click, shift-right-click LIFO removal, dropped-item
 * auto-insertion on top contact, and inventory item spill on block replacement. It also
 * defines a custom non-full-block footprint and horizontal facing for custom model usage.
 *
 * <p>In the broader context of the project, this class provides the physical world-facing
 * entry point for ritual crafting inputs and establishes the interaction contract that
 * later ritual validation and activation systems will consume.
 */
public class ModCorruptedReliquaryBlock extends BaseEntityBlock {

    // ==================================
    //  FIELDS
    // ==================================

    // Codec used by Minecraft to serialize and recreate this block type
    public static final MapCodec<ModCorruptedReliquaryBlock> CODEC =
            simpleCodec(ModCorruptedReliquaryBlock::new);

    // Horizontal facing property for model and shape orientation
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // North/south axis footprint for reliquary collision and selection
    private static final VoxelShape SHAPE_NS = Shapes.or(
            Block.box(3.0D, 0.0D, 2.0D, 13.0D, 4.0D, 14.0D),
            Block.box(4.0D, 4.0D, 3.5D, 12.0D, 9.0D, 12.5D),
            Block.box(2.0D, 9.0D, 1.5D, 14.0D, 12.0D, 14.5D)
    );

    // East/west axis footprint for reliquary collision and selection
    private static final VoxelShape SHAPE_EW = Shapes.or(
            Block.box(2.0D, 0.0D, 3.0D, 14.0D, 4.0D, 13.0D),
            Block.box(3.5D, 4.0D, 4.0D, 12.5D, 9.0D, 12.0D),
            Block.box(1.5D, 9.0D, 2.0D, 14.5D, 12.0D, 14.0D)
    );

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a corrupted reliquary block with the supplied block properties
    public ModCorruptedReliquaryBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // ==================================
    //  BLOCK TYPE
    // ==================================

    // Returns the codec for this block type
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Returns the placement state with facing opposite the player
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // Rotates the block's facing state
    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    // Mirrors the block's facing state
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Adds facing to the blockstate definition
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Returns the custom non-full-block shape based on horizontal axis
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.X ? SHAPE_EW : SHAPE_NS;
    }

    // Returns the collision shape to match the visible reliquary footprint
    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    // ==================================
    //  INTERACTIONS
    // ==================================

    // Handles right-click insertion when the player is holding an item
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stackInHand, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ModCorruptedReliquaryBlockEntity reliquary)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stackInHand.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (level.isClientSide) {
            return reliquary.canAcceptInsert()
                    ? ItemInteractionResult.SUCCESS
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean inserted = reliquary.tryInsertSingle(stackInHand);
        if (!inserted) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!player.getAbilities().instabuild) {
            stackInHand.shrink(1);
        }

        return ItemInteractionResult.SUCCESS;
    }

    // Handles shift-right-click removal when not using an item on the block
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ModCorruptedReliquaryBlockEntity reliquary)) {
            return InteractionResult.PASS;
        }

        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack removed = reliquary.popMostRecentItem();
        if (removed.isEmpty()) {
            return InteractionResult.PASS;
        }

        boolean added = player.addItem(removed);
        if (!added) {
            Block.popResource(level, pos.above(), removed);
        }

        return InteractionResult.SUCCESS;
    }

    // Auto-inserts dropped item entities that contact the top of the reliquary
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (level.isClientSide) return;
        if (!(entity instanceof ItemEntity itemEntity)) return;
        if (itemEntity.getY() < pos.getY() + 0.55D) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ModCorruptedReliquaryBlockEntity reliquary)) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        int inserted = reliquary.tryInsertAsMany(stack);
        if (inserted <= 0) return;

        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
    }

    // ==================================
    //  BLOCK ENTITY
    // ==================================

    // Creates the corrupted reliquary block entity
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModCorruptedReliquaryBlockEntity(pos, state);
    }

    // Drops all stored items when the block is replaced by a different block
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ModCorruptedReliquaryBlockEntity reliquary) {
                for (ItemStack stack : reliquary.copyItems()) {
                    if (!stack.isEmpty()) {
                        Block.popResource(level, pos, stack);
                    }
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
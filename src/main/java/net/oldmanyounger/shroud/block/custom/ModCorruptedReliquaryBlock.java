package net.oldmanyounger.shroud.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.oldmanyounger.shroud.block.entity.ModCorruptedReliquaryBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Block implementation for the Corrupted Reliquary item-input structure.
 *
 * <p>This block routes world interactions into reliquary storage behavior, including
 * single-item insertion on right click, shift-right-click LIFO removal, and inventory
 * item spill on block replacement. It intentionally does not execute ritual logic yet,
 * matching the staged implementation plan.
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

    // ==================================
    //  CONSTRUCTOR
    // ==================================

    // Creates a corrupted reliquary block with the supplied block properties
    public ModCorruptedReliquaryBlock(Properties properties) {
        super(properties);
    }

    // ==================================
    //  BLOCK TYPE
    // ==================================

    // Returns the codec for this block type
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
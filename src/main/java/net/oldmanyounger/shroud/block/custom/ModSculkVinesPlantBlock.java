package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.function.Supplier;

public class ModSculkVinesPlantBlock extends CaveVinesPlantBlock {

    private final Supplier<GrowingPlantHeadBlock> headBlock;

    public ModSculkVinesPlantBlock(BlockBehaviour.Properties properties, Supplier<GrowingPlantHeadBlock> headBlock) {
        super(properties);
        this.headBlock = headBlock;
    }

    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return headBlock.get();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(CaveVines.BERRIES)) {
            return InteractionResult.PASS;
        }

        popResource(level, pos, new ItemStack(ModItems.GLOOM_PULP.get()));
        float pitch = 0.8F + level.random.nextFloat() * 0.4F;
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);
        level.setBlock(pos, state.setValue(CaveVines.BERRIES, false), 2);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
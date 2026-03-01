package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

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

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(CaveVines.BERRIES)) {
            return CaveVines.use(player, state, level, pos);
        }
        return InteractionResult.PASS;
    }
}
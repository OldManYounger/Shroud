package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.function.Supplier;

public class ModSculkVinesBlock extends CaveVinesBlock {

    private final Supplier<GrowingPlantBodyBlock> plantBlock;

    public ModSculkVinesBlock(BlockBehaviour.Properties properties, Supplier<GrowingPlantBodyBlock> plantBlock) {
        super(properties);
        this.plantBlock = plantBlock;
    }

    @Override
    protected GrowingPlantBodyBlock getBodyBlock() {
        return plantBlock.get();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.GLOOM_PULP.get());
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(CaveVines.BERRIES)) {
            return CaveVines.use(player, state, level, pos);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
    }
}
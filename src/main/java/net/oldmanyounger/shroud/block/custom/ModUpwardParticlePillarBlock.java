package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.oldmanyounger.shroud.block.entity.ModBlockEntities;
import net.oldmanyounger.shroud.block.entity.SculkEmitterBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ModUpwardParticlePillarBlock extends net.minecraft.world.level.block.RotatedPillarBlock implements EntityBlock {

    public ModUpwardParticlePillarBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SculkEmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;

        return type == ModBlockEntities.SCULK_EMITTER.get()
                ? (lvl, p, st, be) -> ((SculkEmitterBlockEntity) be).serverTick()
                : null;
    }
}

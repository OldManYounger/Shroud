package net.oldmanyounger.shroud.entity.goal;

import net.minecraft.core.BlockPos;

// Inspiration taken from KyaniteMods (DeeperAndDarker)
public interface VibrationListener {
    BlockPos getVibrationLocation();
    void setVibrationLocation(BlockPos pos);
}
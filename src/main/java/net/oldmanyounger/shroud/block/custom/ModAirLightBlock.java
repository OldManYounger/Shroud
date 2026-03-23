package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Defines an invisible, air-like light source block used by Shroud wherever
 * illumination is needed without placing a visible model or solid block in the
 * world.
 *
 * <p>This class behaves like air rather than a conventional light-emitting
 * block. It has no collision, no occlusion, can be replaced easily, and is
 * effectively strengthless. In the broader context of the project, this makes it
 * useful for subtle ambient lighting, atmosphere control, or hidden technical
 * placement where the mod wants an area to glow without exposing an obvious
 * block to the player.
 *
 * <p>The only custom behavior added here is a configurable light emission value,
 * clamped to Minecraft's normal 0-15 range so the block always remains valid as
 * a light source.
 */
public class ModAirLightBlock extends AirBlock {

    // Stored light level emitted by this otherwise invisible air-like block
    private final int lightLevel;

    // Creates a new invisible light block with a clamped vanilla light value
    public ModAirLightBlock(int lightLevel) {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .noOcclusion()
                .replaceable()
                // Hardness is irrelevant here because the block is meant to behave like air
                .strength(0.0F)
        );

        // Clamp the provided light level to Minecraft's valid 0-15 range
        this.lightLevel = Math.max(0, Math.min(15, lightLevel));
    }

    // Returns the fixed light level emitted by this block
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return this.lightLevel;
    }
}
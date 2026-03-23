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

/**
 * Defines the body block for Shroud's custom sculk vine plant, mirroring vanilla
 * cave vine plant behavior while swapping the harvest result to the mod's custom
 * item.
 *
 * <p>This is the non-head segment of the vine structure and is responsible for
 * linking back to the correct vine head block for growth behavior. It also
 * supports the same berry-harvesting interaction as the head, ensuring the full
 * vine chain remains interactive and consistent for the player.
 *
 * <p>In the broader context of the project, this block supports harvestable,
 * vertically growing flora that reinforces the sculk ecosystem and gives the
 * mod's environments more interactive plant life.
 */
public class ModSculkVinesPlantBlock extends CaveVinesPlantBlock {

    // Reference to the matching vine head block used for growth logic
    private final Supplier<GrowingPlantHeadBlock> headBlock;

    // Creates the sculk vine body block and stores the linked head block supplier
    public ModSculkVinesPlantBlock(BlockBehaviour.Properties properties, Supplier<GrowingPlantHeadBlock> headBlock) {
        super(properties);
        this.headBlock = headBlock;
    }

    // Returns the vine head block associated with this plant body block
    @Override
    protected GrowingPlantHeadBlock getHeadBlock() {
        return headBlock.get();
    }

    // Handles harvesting when this vine body segment currently has berries
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Do nothing if this vine segment does not currently hold berries
        if (!state.getValue(CaveVines.BERRIES)) {
            return InteractionResult.PASS;
        }

        // Drop the custom sculk vine harvest item
        popResource(level, pos, new ItemStack(ModItems.GLOOM_PULP.get()));

        // Play the standard berry harvesting sound with small random pitch variation
        float pitch = 0.8F + level.random.nextFloat() * 0.4F;
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);

        // Remove berries from this vine segment after harvesting
        level.setBlock(pos, state.setValue(CaveVines.BERRIES, false), 2);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
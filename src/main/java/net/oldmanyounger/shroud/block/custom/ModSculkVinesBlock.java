package net.oldmanyounger.shroud.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.BlockHitResult;
import net.oldmanyounger.shroud.item.ModItems;

import java.util.function.Supplier;

/**
 * Defines the head block for Shroud's custom sculk vine plant, adapting vanilla
 * cave vine behavior to use the mod's own harvest item and linked body block.
 *
 * <p>This class preserves the familiar vine interaction loop players expect:
 * berries can appear on the vine, players can harvest them by interacting with
 * the block, and the vine then resets to a non-fruiting state. The main
 * customization is that harvesting yields {@code GLOOM_PULP} instead of a
 * vanilla cave vine drop.
 *
 * <p>In the broader context of the project, this block helps integrate organic,
 * harvestable sculk vegetation into Shroud's environmental systems while still
 * leaning on stable vanilla plant-growth behavior.
 */
public class ModSculkVinesBlock extends CaveVinesBlock {

    // Reference to the matching vine body block used by this growing plant
    private final Supplier<GrowingPlantBodyBlock> plantBlock;

    // Creates the sculk vine head block and stores the linked body block supplier
    public ModSculkVinesBlock(BlockBehaviour.Properties properties, Supplier<GrowingPlantBodyBlock> plantBlock) {
        super(properties);
        this.plantBlock = plantBlock;
    }

    // Returns the body block that should be used for this vine structure
    @Override
    protected GrowingPlantBodyBlock getBodyBlock() {
        return plantBlock.get();
    }

    // Controls the item returned by pick block / clone interactions
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.GLOOM_PULP.get());
    }

    // Handles harvesting of the vine's fruit when the player interacts empty-handed
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        // Do nothing if this vine currently has no berries to harvest
        if (!state.getValue(CaveVines.BERRIES)) {
            return InteractionResult.PASS;
        }

        // Drop the custom harvest item
        popResource(level, pos, new ItemStack(ModItems.GLOOM_PULP.get()));

        // Play the standard cave vine berry-picking sound with slight pitch variation
        float pitch = 0.8F + level.random.nextFloat() * 0.4F;
        level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, pitch);

        // Clear the berry state after harvesting
        level.setBlock(pos, state.setValue(CaveVines.BERRIES, false), 2);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // Defers random ticking behavior to the parent cave vine implementation
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
    }
}